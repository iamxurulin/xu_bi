import {genChartByAiUsingPost} from '@/services/xubi/chartController';
import {UploadOutlined} from '@ant-design/icons';
import {App,Button, Card, Col, Divider, Form, Input, message, Row, Select, Space, Spin, Upload} from 'antd';
import TextArea from 'antd/es/input/TextArea';
import React, {useState} from 'react';

import ReactECharts from 'echarts-for-react';

/**
 * 添加图表页面
 *
 * @constructor
 */
const AddChart: React.FC = () => {
  //  使用 useApp 获取上下文感知的 message 实例
  const { message } = App.useApp();
  //定义状态，用来接收后端的返回值，让它实时展示在页面上
  const [chart,setChart] = useState<API.BiResponse>();
  const [option,setOption] = useState<any>();

  //提交中的状态，默认未提交
  const [submitting,setSubmitting] = useState<boolean>(false);
  const normFile = (e: any) => {
    if (Array.isArray(e)) {
      return e;
    }
    return e?.fileList;
  };

  /**
   * 提交
   *
   * @param values
   */
  const onFinish = async (values: any) => {

    // 校验文件是否已上传
    if (!values.file || values.file.length === 0) {
      message.error('请先上传原始数据文件');
      return;
    }

    //如果已经是提交中的状态（还在加载），直接返回，避免重复提交
    if(submitting){
      return;
    }

    //当开始提交，把submitting设置为true
    setSubmitting(true);

    //如果提交了，把图表数据和图表代码清空掉，防止和之前提交的图标堆叠在一起
    //如果option清空了，组件就会触发重新渲染，就不会保留之前的历史记录
    setChart(undefined);
    setOption(undefined);

    //对接后端，上传数据
    const  params = {
      ...values,
      file: undefined,
    };

    try{
      //需要取到上传的原始数据file->file->originFileObj(原始数据)
      const res = await genChartByAiUsingPost(params, {}, values.file[0].originFileObj);
      //正常情况下，如果没有返回值就分析失败，有，就分析成功
      if(!res?.data){
        message.error('分析失败');
      }else{
        message.success('分析成功');

        const rawGenChart = res.data.genChart ?? '';

        // 1. 寻找 JSON 的“左大括号”和“右大括号”的位置
        const jsonStart = rawGenChart.indexOf('{');
        const jsonEnd = rawGenChart.lastIndexOf('}');

        let chartOption;

        if (jsonStart !== -1 && jsonEnd !== -1) {
          // 2. 截取这两个括号中间的内容（包含括号）
          // 这样可以完美避开 AI 生成的 ```json ... ``` 或者其他多余文字
          const jsonString = rawGenChart.substring(jsonStart, jsonEnd + 1);

          try {
            // 3. 尝试解析截取后的字符串
            chartOption = JSON.parse(jsonString);
          } catch (e) {
            console.error('JSON截取后解析依然失败:', jsonString);
            throw new Error('JSON 格式错误');
          }
        } else {
          throw new Error('AI 未返回有效的 JSON 格式');
        }

        //解析成对象，为空则设为空字符串
        //const chartOption = JSON.parse(res.data.genChart?? '');

        //如果为空，则抛出异常，并提示‘图表代码解析错误’
        if(!chartOption){
          throw new Error('图表代码解析错误')
          //如果成功
        }else{
          //从后端得到响应结果之后，把响应结果设置到图表状态里
          setChart(res.data);
          setOption(chartOption);
        }
      }

      //异常情况下，提示分析失败+具体失败原因

    }catch (e:any){
      message.error('分析失败，'+e.message);
    }

    //当结束提交，把submitting设置为false
    setSubmitting(false);
  };

  return (
    // 把页面内容指定一个类名add-chart
    <div className="add-chart">
      {/* 变成两列 gutter 列与列之间的间隔*/}
      <Row gutter={24}>
        {/* 表单放在第一列，卡片组件里 */}
        <Col span={12}>
          <Card title="智能分析">
            <Form
              // 表单名称为addChart
              name="addChart"

              //label标签的文本对齐方式
              labelAlign="left"

              //label标签布局，同<Col>组件，设置 span offset 值，如 { span: 3,offset: 12}
              labelCol = {{ span : 4}}

              //设置控件布局样式
              wrapperCol= {{ span: 16}}
              onFinish={onFinish}

              // 初始化数据啥都不填，为空
              initialValues={{  }}
            >
              {/* 前端表单的name，对应后端接口请求参数里的字段，
                此处name对应后端分析目标goal,label是左侧的提示文本，
                rules=....是必填项提示*/}
              <Form.Item name="goal" label="分析目标" rules={[{ required: true, message: '请输入分析目标!' }]}>
                {/* placeholder文本框内的提示语 */}
                <TextArea placeholder="请输入你的分析需求，比如：分析网站用户的增长情况"/>
              </Form.Item>

              {/* 还要输入图表名称 */}
              <Form.Item name="name" label="图表名称">
                <Input placeholder="请输入图表名称" />
              </Form.Item>

              {/* 图表类型是非必填，所以不做校验 */}
              <Form.Item
                name="chartType"
                label="图表类型"
              >
                <Select
                  options={[
                    { value: '折线图', label: '折线图' },
                    { value: '柱状图', label: '柱状图' },
                    { value: '堆叠图', label: '堆叠图' },
                    { value: '饼图', label: '饼图' },
                    { value: '雷达图', label: '雷达图' },
                  ]}
                />
              </Form.Item>

              {/* 文件上传 */}
              <Form.Item
                name="file"
                label="原始数据"
                valuePropName="fileList"
                getValueFromEvent={normFile}
              >
                {/* action:当你把文件上传之后，他会把文件上传至哪个接口。
                这里肯定是调用自己的后端，先不用这个;
                 maxCount = {1} 限制文件上传数量为1 */}

                <Upload name="file" maxCount = {1}>
                  <Button icon={<UploadOutlined />}>上传 CSV 文件</Button>
                </Upload>
              </Form.Item>

              {/* offset 设置和label标签一样的宽度，这样就能保持对齐；
                  其他占用的列设置成16*/}
              <Form.Item wrapperCol={{ span: 16, offset: 4 }}>
                <Space>
                  <Button type="primary" htmlType="submit" loading={submitting} disabled={submitting}>
                    提交
                  </Button>
                  <Button htmlType="reset">重置</Button>
                </Space>
              </Form.Item>
            </Form>
          </Card>
        </Col>

        {/* 分析结论和图表放在第二列 */}
        <Col span={12}>
          <Card title = "分析结论">
            {chart?.genResult??<div> 请先在左侧进行提交</div>}

            <Spin spinning = {submitting}/>

          </Card>

          <Divider />

          <Card title="可视化图表">
            {
              option ? <ReactECharts option={option} /> : <div> 请先在左侧进行提交</div>
            }

            <Spin spinning = {submitting}/>
          </Card>
        </Col>
      </Row>

    </div>

  );
};
export default AddChart;
