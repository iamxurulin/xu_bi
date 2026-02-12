import {listMyChartByPageUsingPost} from '@/services/xubi/chartController';
import {Avatar, Card, List, message, Result} from 'antd';
import React, {useEffect, useState} from 'react';
import ReactECharts from "echarts-for-react";
import {useModel} from "@@/exports";
import Search from "antd/es/input/Search";

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

                      <div style={{ width: '100%', height: '350px' }}>
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
    </div>
  );
};
export default MyChartPage;
