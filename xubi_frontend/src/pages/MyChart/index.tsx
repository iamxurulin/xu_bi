import {listMyChartByPageUsingPost, getChartDataByIdUsingGet, retryFailedChartUsingPost, deleteChartUsingPost, editChartUsingPost, getChartByIdUsingGet} from '@/services/xubi/chartController';
import {Avatar, Card, List, message, Result, Modal, Button, Table, Spin, Switch, Form, Input, Select} from 'antd';
import React, {useEffect, useState, useRef, useCallback} from 'react';
import ReactECharts from "echarts-for-react";
import {useModel} from "@@/exports";
import Search from "antd/es/input/Search";
import { csvToTable } from '@/utils/csv';

/**
 * 我的图表页面
 *
 * @constructor
 */
/**
 * Markdown 简易渲染组件
 * 将常见的 Markdown 语法转为 HTML，无需额外依赖
 */
const MarkdownContent: React.FC<{ text?: string }> = ({ text }) => {
  if (!text) return <span>暂无分析结论</span>;

  // 清理：去掉 AI 冗余前缀和首尾花括号
  let cleaned = text.trim();
  cleaned = cleaned.replace(/^结论[：:]\s*/, '');
  // 去掉开头多余的单个 { 字符
  if (cleaned.startsWith('{')) {
    cleaned = cleaned.slice(1).trim();
  }

  const html = cleaned
    // 标题：# ## ###
    .replace(/^### (.*$)/gim, '<h3>$1</h3>')
    .replace(/^## (.*$)/gim, '<h2>$1</h2>')
    .replace(/^# (.*$)/gim, '<h1>$1</h1>')
    // 粗体：**text**
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    // 斜体：*text*
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    // 无序列表：- item
    .replace(/^- (.*$)/gim, '<li>$1</li>')
    // 有序列表：1. item
    .replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
    // 段落：空行分隔
    .replace(/\n\n/g, '</p><p>')
    // 换行
    .replace(/\n/g, '<br>');

  return (
    <div
      style={{ color: '#333' }}
      dangerouslySetInnerHTML={{ __html: `<p>${html}</p>` }}
    />
  );
};

const MyChartPage: React.FC = () => {

  //把初始条件分离出来，便于后面恢复初始条件
  const initSearchParams = {
    //默认第一页
    current: 1,
    //每页展示4条数据
    pageSize: 4,
    //设置按创建时间排序
    sortField: 'createTime',
    sortOrder: 'desc',
  };
  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({...initSearchParams});

  const {initialState} = useModel('@@initialState');
  const {currentUser} = initialState ?? {};

  const [chartList, setChartList] = useState<API.Chart[]>();

  const [total, setTotal] = useState<number>(0);

  const [loading, setLoading] = useState<boolean>(true);

  // 控制查看原始数据弹窗
  const [isModalVisible, setIsModalVisible] = useState<boolean>(false);
  // 当前选中图表的原始 CSV 字符串
  const [currentChartData, setCurrentChartData] = useState<string>('');
  // 请求原始数据加载状态（可配合 fetchingId 做单卡片 loading）
  const [loadingData, setLoadingData] = useState<boolean>(false);
  const [fetchingId, setFetchingId] = useState<number | null>(null);
  // Table 数据
  const [tableColumns, setTableColumns] = useState<any[]>([]);
  const [tableDataSource, setTableDataSource] = useState<any[]>([]);
  // 表尾汇总（如班级平均分/最高分/最低分）
  const [tableSummaryRows, setTableSummaryRows] = useState<any[]>([]);

  // 重试按钮加载状态
  const [retryLoading, setRetryLoading] = useState<boolean>(false);
  // 当前正在重试的图表ID
  const [retryingId, setRetryingId] = useState<number | null>(null);

  // 重新生成按钮加载状态
  const [regenerateLoading, setRegenerateLoading] = useState<boolean>(false);
  const [regeneratingId, setRegeneratingId] = useState<number | null>(null);

  // 编辑图表弹窗
  const [isEditModalVisible, setIsEditModalVisible] = useState<boolean>(false);
  const [editingChart, setEditingChart] = useState<API.Chart | null>(null);
  const [editingLoading, setEditingLoading] = useState<boolean>(false);
  const [editForm] = Form.useForm();

  // === 自动轮询相关状态 ===
  const [autoRefresh, setAutoRefresh] = useState<boolean>(true);
  const pollingTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  // 使用 ref 追踪是否有待处理任务，避免 state 闭包过期问题
  const hasPendingRef = useRef<boolean>(false);

  const loadData = async (polling = false, useMaxPageSize = false) => {
    if (polling) {
      // 轮询时不显示 loading 状态和错误提示，避免页面闪烁
    } else {
      setLoading(true);
    }
    try {
      let params = searchParams;
      if (useMaxPageSize) {
        // 轮询时使用最大 pageSize，确保覆盖所有图表，避免分页导致漏掉状态更新
        params = { ...searchParams, pageSize: 20 };
      }
      const res = await listMyChartByPageUsingPost(params);


      if (res.data) {
        setChartList(res.data.records??[]);
        setTotal(res.data.total ?? 0);
      } else if (!polling) {
        message.error('获取我的图表失败');
      }

    } catch (e: any) {
      if (!polling) {
        message.error('获取我的图表失败，' + e.message);
      }
    } finally {
      if (!polling) {
        setLoading(false);
      }
    }
  };

  /** 启动轮询 */
  const startPolling = useCallback(() => {
    stopPolling();
    if (!autoRefresh || !hasPendingRef.current) return;
    pollingTimerRef.current = setInterval(() => {
      loadData(true, true);
    }, 3000);
  }, [autoRefresh]);

  /** 停止轮询 */
  const stopPolling = useCallback(() => {
    if (pollingTimerRef.current) {
      clearInterval(pollingTimerRef.current);
      pollingTimerRef.current = null;
    }
  }, []);

  // 首次页面加载时，触发加载数据
  useEffect(() => {
    loadData();
  }, [searchParams]);

  // 数据加载完成后判断是否需要轮询
  useEffect(() => {
    // 更新 ref，供 startPolling 使用
    hasPendingRef.current = chartList?.some(
      (item) => item.status === 'wait' || item.status === 'running',
    ) ?? false;

    startPolling();
    return stopPolling;
  }, [chartList, autoRefresh, startPolling, stopPolling]);

  // 使用 utils 中的 csvToTable 返回 columns/dataSource

  /**
   * 点击“查看原始数据”按钮时调用，按 id 请求后端 CSV 文本
   */
  const handleViewData = async (id?: number) => {
    if (!id) {
      message.error('图表 ID 不存在');
      return;
    }
    setLoadingData(true);
    setFetchingId(id);
    try {
      const res = await getChartDataByIdUsingGet({ id });
      if (res && (res as any).data !== undefined) {
        const csvText = (res as any).data ?? '';
          setCurrentChartData(csvText);
          const { columns, dataSource, summary } = csvToTable(csvText);
          setTableColumns(columns);
          setTableDataSource(dataSource);
          setTableSummaryRows(summary || []);
          setIsModalVisible(true);
      } else {
        message.error('获取原始数据失败');
      }
    } catch (e: any) {
      message.error('获取原始数据失败，' + e?.message);
    }
    setLoadingData(false);
    setFetchingId(null);
  };

  /**
   * 点击"删除"按钮时调用，删除指定图表
   */
  const handleDeleteChart = (id?: number) => {
    if (!id) {
      message.error('图表 ID 不存在');
      return;
    }
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除此图表吗？删除后将无法恢复。',
      okText: '确认删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        try {
          const res = await deleteChartUsingPost({ id });
          if (res.data) {
            message.success('删除成功');
            loadData();
          } else {
            message.error('删除失败');
          }
        } catch (e: any) {
          message.error('删除失败，' + e?.message);
        }
      },
    });
  };

  /**
   * 点击"重试"按钮时调用，重试生成失败的图表
   */
  const handleRetryChart = (id?: number) => {
    if (!id) {
      message.error('图表 ID 不存在');
      return;
    }
    // 弹出确认框
    Modal.confirm({
      title: '确认重试',
      content: '确定要重试生成此图表吗？',
      onOk: async () => {
        setRetryLoading(true);
        setRetryingId(id);
        try {
          const res = await retryFailedChartUsingPost({ id });
          if (res.data) {
            message.success('重试任务已提交');
            // 刷新列表
            loadData();
          } else {
            message.error('重试失败');
          }
        } catch (e: any) {
          message.error('重试失败，' + e?.message);
        } finally {
          setRetryLoading(false);
          setRetryingId(null);
        }
      },
    });
  };

  /**
   * 点击"编辑"按钮时调用，弹出编辑弹窗
   */
  const handleEditChart = async (id?: number) => {
    if (!id) {
      message.error('图表 ID 不存在');
      return;
    }
    try {
      const res = await getChartByIdUsingGet({ id });
      if (res?.data) {
        setEditingChart(res.data);
        editForm.setFieldsValue({
          name: res.data.name,
          goal: res.data.goal,
          chartType: res.data.chartType,
        });
        setIsEditModalVisible(true);
      } else {
        message.error('获取图表详情失败');
      }
    } catch (e: any) {
      message.error('获取图表详情失败，' + e?.message);
    }
  };

  /**
   * 提交编辑表单
   */
  const handleEditSubmit = async () => {
    try {
      const values = await editForm.validateFields();
      if (!editingChart?.id) return;
      setEditingLoading(true);
      const res = await editChartUsingPost({
        id: editingChart.id,
        name: values.name,
        goal: values.goal,
        chartType: values.chartType,
      });
      if (res.data) {
        message.success('编辑成功');
        setIsEditModalVisible(false);
        loadData();
      } else {
        message.error('编辑失败');
      }
    } catch (e: any) {
      if (e?.message) {
        message.error('编辑失败，' + e.message);
      }
    } finally {
      setEditingLoading(false);
    }
  };

  /**
   * 点击"重新生成"按钮时调用，复用 retry 接口将图表重新提交到 MQ
   */
  const handleRegenerateChart = async (id?: number) => {
    if (!id) {
      message.error('图表 ID 不存在');
      return;
    }
    Modal.confirm({
      title: '确认重新生成',
      content: '重新生成将覆盖当前图表结果，确定继续吗？',
      okText: '确认重新生成',
      okButtonProps: { danger: true },
      cancelText: '取消',
      onOk: async () => {
        setRegenerateLoading(true);
        setRegeneratingId(id);
        try {
          const res = await retryFailedChartUsingPost({ id });
          if (res.data) {
            message.success('重新生成任务已提交');
            loadData();
          } else {
            message.error('重新生成失败');
          }
        } catch (e: any) {
          message.error('重新生成失败，' + e?.message);
        } finally {
          setRegenerateLoading(false);
          setRegeneratingId(null);
        }
      },
    });
  };

  return (
    // 把页面内容指定一个类名add-chart
    <div className="my-chart-page">

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
        <Search placeholder="请输入图表名称" enterButton loading={loading}
                style={{ width: '50%', maxWidth: 400 }}
                onSearch={(value) => {
                  //设置搜索条件
                  setSearchParams({
                    ...initSearchParams,
                    name: value,
                  })
                }}/>
        <div style={{ display: 'inline-flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: 14, color: '#666' }}>自动刷新</span>
          <Switch
            checked={autoRefresh}
            onChange={(val) => {
              setAutoRefresh(val);
              if (!val) {
                stopPolling();
              } else {
                startPolling();
              }
            }}
          />
          {autoRefresh && hasPendingRef.current && (
            <span style={{ fontSize: 12, color: '#faad14', whiteSpace: 'nowrap' }}>● 轮询中</span>
          )}
        </div>
      </div>
      <List
        grid={{
          gutter: 16,
          xs: 1,
          sm: 1,
          md: 1,
          lg: 1,
          xl: 1,
          xxl: 1,
        }}
        pagination={{
          onChange: (page, pageSize) => {
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize,
            })
          },

          current: searchParams.current,
          pageSize: searchParams.pageSize,
          total: total,
        }}

        loading={loading}
        dataSource={chartList}

        renderItem={(item) => (
            <List.Item key={item.id}>
              <Card style={{width: '100%', position: 'relative'}}>
                <List.Item.Meta
                  avatar={<Avatar src={currentUser && currentUser.userAvatar}/>}
                  title={item.name}
                  description={item.chartType ? '图表类型：' + item.chartType : undefined}
                />
                <div style={{marginBottom: 16}}/>

                <>
                  {
                    item.status === 'wait' && <>
                      <Result
                        status="warning"
                        title="待生成"
                        subTitle={item.execMessage ?? '当前图表生成队列繁忙，请耐心等候'}
                      />
                      <div style={{ position: 'absolute', right: 12, top: 12, zIndex: 1000 }}>
                        <Button size="small" type="primary" onClick={() => handleRetryChart(item.id as number)}
                                loading={retryLoading && retryingId === (item.id as number)}>
                          重试
                        </Button>
                      </div>
                    </>
                  }
                  {
                    item.status === 'running' && <>

                      <Result
                        status="info"
                        title="图表生成中"
                        subTitle={item.execMessage}
                      />
                    </>
                  }
                  {item.status === 'succeed' && (
                    <>
                      <div style={{ marginBottom: 16 }}>
                        <span style={{ fontWeight: 'bold' }}>分析目标：</span>
                        {item.goal}
                      </div>

                              <div style={{ width: '100%', height: '350px', position: 'relative' }}>
                        {/* 使用立即执行函数来处理 try-catch */}
                        {(() => {
                          try {
                            // 1. 获取原始字符串
                            const rawGenChart = item.genChart ?? '{}';
                            // 2. 截取 {} 中间的内容 (自动过滤首尾的单引号、Markdown标记等)
                            const jsonStart = rawGenChart.indexOf('{');
                            const jsonEnd = rawGenChart.lastIndexOf('}');

                            if (jsonStart !== -1 && jsonEnd !== -1) {
                              const jsonString = rawGenChart.substring(jsonStart, jsonEnd + 1);
                              const chartOption = JSON.parse(jsonString);
                              // 移除 title 防止重复
                              chartOption.title = undefined;
                              return <ReactECharts option={chartOption} style={{ height: '100%' }} />;
                            }
                            throw new Error('JSON 格式无效');
                          } catch (e) {
                            return <Result status="error" title="图表解析失败" subTitle="AI 生成的数据格式有误" />;
                          }
                        })()}
                      </div>

                      {/* 分析结论 */}
                      <div style={{ marginBottom: 16 }}>
                        <span style={{ fontWeight: 'bold' }}>分析结论：</span>
                        <div style={{ marginTop: 8, lineHeight: 1.8 }}>
                          <MarkdownContent text={item.genResult} />
                        </div>
                      </div>

                      {/* 右上角按钮组 */}
                      <div style={{ position: 'absolute', right: 12, top: 12, zIndex: 1000, display: 'flex', gap: 8 }}>
                        <Button size="small" onClick={() => handleEditChart(item.id as number)}>
                          编辑
                        </Button>
                        <Button size="small" onClick={() => handleRegenerateChart(item.id as number)}
                                loading={regenerateLoading && regeneratingId === (item.id as number)}>
                          重新生成
                        </Button>
                        <Button size="small" onClick={() => handleDeleteChart(item.id as number)} danger>
                          删除
                        </Button>
                        <Button size="small" onClick={() => handleViewData(item.id as number)}
                                loading={loadingData && fetchingId === (item.id as number)}>
                          查看原始数据
                        </Button>
                      </div>
                    </>
                  )}
                  {
                    item.status === 'failed' && <>

                      <Result
                        status="error"
                        title="图表生成失败"
                        subTitle={item.execMessage}
                      />
                      <div style={{ position: 'absolute', right: 12, top: 12, zIndex: 1000 }}>
                        <Button size="small" onClick={() => handleRetryChart(item.id as number)}
                                loading={retryLoading && retryingId === (item.id as number)}>
                          重试
                        </Button>
                      </div>
                    </>
                  }
                </>
              </Card>
            </List.Item>
          )}
      />

      {/* 原始数据 Modal */}
      <Modal
        title="原始数据"
        open={isModalVisible}
        onCancel={() => setIsModalVisible(false)}
        footer={null}
        width={900}
      >
        {loadingData ? (
          <div style={{ textAlign: 'center', padding: 24 }}>
            <Spin />
          </div>
        ) : (
          <>
            {tableColumns && tableColumns.length > 0 ? (
              <>
                <Table
                  columns={tableColumns}
                  dataSource={tableDataSource}
                  pagination={{ pageSize: 50 }}
                  // 允许横向滚动以支持宽列与手动拉伸/查看完整内容
                  scroll={{ x: tableColumns.reduce((s, c) => s + (c.width || 120), 0), y: 300 }}
                  size="small"
                />
                {/* 如果存在汇总行，单独展示在表格下方 */}
                {tableSummaryRows && tableSummaryRows.length > 0 && (
                  <div style={{ marginTop: 12 }}>
                    <div style={{ fontWeight: 600, marginBottom: 8 }}>汇总</div>
                    <Table
                      columns={tableColumns}
                      dataSource={tableSummaryRows}
                      pagination={false}
                      // 同步使用横向滚动，使汇总列不被截断
                      scroll={{ x: tableColumns.reduce((s, c) => s + (c.width || 120), 0) }}
                      size="small"
                    />
                  </div>
                )}
              </>
            ) : (
              <pre style={{ maxHeight: 400, overflow: 'auto' }}>{currentChartData || '无原始数据'}</pre>
            )}
          </>
        )}
      </Modal>

      {/* 编辑图表弹窗 */}
      <Modal
        title="编辑图表"
        open={isEditModalVisible}
        onCancel={() => {
          setIsEditModalVisible(false);
          editForm.resetFields();
        }}
        onOk={handleEditSubmit}
        confirmLoading={editingLoading}
        destroyOnClose
        width={600}
      >
        <Form form={editForm} layout="vertical" style={{ marginTop: 24 }}>
          <Form.Item name="name" label="图表名称" rules={[{ required: true, message: '请输入图表名称' }]}>
            <Input placeholder="请输入图表名称" />
          </Form.Item>
          <Form.Item name="goal" label="分析目标" rules={[{ required: true, message: '请输入分析目标' }]}>
            <Input.TextArea rows={4} placeholder="请输入分析目标" />
          </Form.Item>
          <Form.Item name="chartType" label="图表类型">
            <Select placeholder="请选择图表类型" allowClear>
              <Select.Option value="bar">柱状图</Select.Option>
              <Select.Option value="pie">饼图</Select.Option>
              <Select.Option value="line">折线图</Select.Option>
              <Select.Option value="scatter">散点图</Select.Option>
              <Select.Option value="heatmap">热力图</Select.Option>
              <Select.Option value="radar">雷达图</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};
export default MyChartPage;
