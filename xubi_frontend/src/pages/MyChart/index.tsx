import {listMyChartByPageUsingPost} from '@/services/xubi/chartController';
import {Avatar, Card, List, message} from 'antd';
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
    current: 1,
    pageSize: 4,
  };
  const [searchParams, setSearchParams] = useState<API.ChartQueryRequest>({...initSearchParams});

  const {initialState} = useModel('@@initialState');
  const {currentUser} = initialState ?? {};

  const [chartList,setChartList]= useState<API.Chart[]>();

  const [total,setTotal]=useState<number>(0);

  const [loading,setLoading] = useState<boolean>(true);

  const loadData = async()=>{
    setLoading(true);
    try {
      const res = await listMyChartByPageUsingPost(searchParams);
      console.log('res', res);

      if(res.data){
        setTotal(res.data.total ?? 0);

        if(res.data.records){
          const processedRecords = res.data.records.map(data => {
            try {
              console.log('原始 genChart:', data.genChart);

              let cleanedChart = (data.genChart ?? '{}')
                .trim()                         // 【新增】先去除首尾空格
                .replace(/^['"`]+|['"`]+$/g, '')  // 【关键修改】去除首尾的引号(单引号/双引号/反引号)
                .replace(/['']/g, "'")          // 替换智能单引号
                .replace(/[""]/g, '"')          // 替换智能双引号
                .replace(/[\u2018\u2019]/g, "'")  // Unicode 智能单引号
                .replace(/[\u201C\u201D]/g, '"')  // Unicode 智能双引号
                .replace(/\n/g, '')             // 移除换行符
                .replace(/\r/g, '')             // 移除回车符
                .replace(/\t/g, '')             // 移除制表符
                .trim();                        // 再次去除首尾空格

              console.log('清洗后 genChart:', cleanedChart);

              const chartOption = JSON.parse(cleanedChart);
              chartOption.title = undefined;

              return {
                ...data,
                genChart: JSON.stringify(chartOption)
              };
            } catch (e) {
              console.error('解析图表数据失败:', data.id, e);
              console.error('失败的数据:', data.genChart);
              return {
                ...data,
                genChart: '{}'
              };
            }
          });

          setChartList(processedRecords);
        }
      }else{
        message.error('获取我的图表失败');
      }

    } catch (e:any) {
      message.error('获取我的图表失败，'+e.message);
    }
    setLoading(false);
  };

  //首次页面加载时，触发加载数据
  useEffect(()=>{
    loadData();
  },[searchParams]);

  return (
    // 把页面内容指定一个类名add-chart
    <div className="my-chart-page">

      <div>
        <Search placeholder = "请输入图表名称" enterButton loading = {loading}
                onSearch = {(value)=>{
                  //设置搜索条件
                  setSearchParams({
                    ...initSearchParams,
                    name: value,
                  })
                }}/>
      </div>
      <div className="margin-16"/>
      <List
        grid = {{
          gutter: 16,
          xs: 1,
          sm: 1,
          md: 1,
          lg: 2,
          xl: 2,
          xxl: 2,
        }}
        pagination={{
          onChange:(page, pageSize)=> {
            setSearchParams({
              ...searchParams,
              current: page,
              pageSize,
            })
          },

          current:searchParams.current,
          pageSize:searchParams.pageSize,
          total:total,
        }}

        loading={loading}
        dataSource={chartList}

        renderItem={(item)=>{
          let chartOption = {};
          try {
            chartOption = item.genChart ? JSON.parse(item.genChart) : {};
          } catch (e) {
            console.error('渲染时解析图表失败:', item.id, e);
            chartOption = {};
          }

          return (
            <List.Item key={item.id}>
              <Card style={{width: '100%'}}>
                <List.Item.Meta
                  avatar={<Avatar src={currentUser&&currentUser.userAvatar}/>}
                  title={item.name}
                  description={item.chartType ? '图表类型：' + item.chartType : undefined}
                />
                <div style={{marginBottom: 16}}/>

                <p>{'分析目标：'+item.goal}</p>

                <div style={{marginBottom: 16}}/>

                {Object.keys(chartOption).length > 0 && (
                  <ReactECharts option={chartOption} />
                )}
              </Card>
            </List.Item>
          );
        }}
      />
    </div>
  );
};
export default MyChartPage;
