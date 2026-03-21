import {listMyChartByPageUsingPost, getChartDataUsingGet} from '@/services/xubi/chartController';
import {Avatar, Card, List, message, Result, Modal, Button, Table, Spin} from 'antd';
import React, {useEffect, useState} from 'react';
import ReactECharts from "echarts-for-react";
import {useModel} from "@@/exports";
import Search from "antd/es/input/Search";
import { csvToTable } from '@/utils/csv';

/**
 * 我的图表页面
 *
 * @constructor
 */
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

  const loadData = async () => {
    setLoading(true);
    try {
      const res = await listMyChartByPageUsingPost(searchParams);


      if (res.data) {
        setChartList(res.data.records??[]);
        setTotal(res.data.total ?? 0);

      } else {
        message.error('获取我的图表失败');
      }

    } catch (e: any) {
      message.error('获取我的图表失败，' + e.message);
    }
    setLoading(false);
  };

  //首次页面加载时，触发加载数据
  useEffect(() => {
    loadData();
  }, [searchParams]);

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
      const res = await getChartDataUsingGet(id);
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

  return (
    // 把页面内容指定一个类名add-chart
    <div className="my-chart-page">

      <div>
        <Search placeholder="请输入图表名称" enterButton loading={loading}
                onSearch={(value) => {
                  //设置搜索条件
                  setSearchParams({
                    ...initSearchParams,
                    name: value,
                  })
                }}/>
      </div>
      <div className="margin-16"/>
      <List
        grid={{
          gutter: 16,
          xs: 1,
          sm: 1,
          md: 1,
          lg: 2,
          xl: 2,
          xxl: 2,
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
              <Card style={{width: '100%'}}>
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
                      {/* 左上角按钮 */}
                      <div style={{ position: 'absolute', right: 12, top: 12, zIndex: 1000 }}>
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
        visible={isModalVisible}
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
    </div>
  );
};
export default MyChartPage;
