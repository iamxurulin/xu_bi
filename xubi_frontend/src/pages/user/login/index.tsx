import { Footer } from '@/components';
import {
  LockOutlined,
  UserOutlined,
} from '@ant-design/icons';
import {
  LoginForm,
  ProFormText,
} from '@ant-design/pro-components';
import {Helmet, history, Link, useModel} from '@umijs/max';
import { Alert, App, Tabs } from 'antd';
import { createStyles } from 'antd-style';
import React, {useEffect, useState} from 'react';
import { flushSync } from 'react-dom';
import Settings from '../../../../config/defaultSettings';
import {listChartByPageUsingPost} from "@/services/xubi/chartController";
import {getLoginUserUsingGet, userLoginUsingPost} from "@/services/xubi/userController";
const useStyles = createStyles(({ token }) => {
  return {
    action: {
      marginLeft: '8px',
      color: 'rgba(0, 0, 0, 0.2)',
      fontSize: '24px',
      verticalAlign: 'middle',
      cursor: 'pointer',
      transition: 'color 0.3s',
      '&:hover': {
        color: token.colorPrimaryActive,
      },
    },
    lang: {
      width: 42,
      height: 42,
      lineHeight: '42px',
      position: 'fixed',
      right: 16,
      borderRadius: token.borderRadius,
      ':hover': {
        backgroundColor: token.colorBgTextHover,
      },
    },
    container: {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    },
  };
});




const Login: React.FC = () => {
  const [type, setType] = useState<string>('account');
  const { initialState, setInitialState } = useModel('@@initialState');
  const { styles } = useStyles();
  const { message } = App.useApp();

  useEffect(()=>{
    listChartByPageUsingPost({}).then(res=>{
      console.error('res',res)
    })
  },[])


  const fetchUserInfo = async () => {
    //调用后端实际的接口，获取到用户信息
    const userInfo = await getLoginUserUsingGet();
    if (userInfo.data) {
      flushSync(() => {
        setInitialState((s) => ({
          ...s,
          currentUser: userInfo.data,
        }));
      });
    }
  };
  const handleSubmit = async (values: API.UserLoginRequest) => {
    try {
      // 登录
      const res = await userLoginUsingPost(values);
      //如果响应值的code为0，则登录成功
      if (res.code === 0) {
        const defaultLoginSuccessMessage = '登录成功！';
        //并弹窗提示登录成功
        message.success(defaultLoginSuccessMessage);
        //登录成功之后获取当前用户的信息
        await fetchUserInfo();
        //跳转回登录前的页面
        const urlParams = new URL(window.location.href).searchParams;
        /**
         * window.location.href，这会导致浏览器整页刷新，
         * 导致 React 状态丢失，体验也不好。改为 history.push。
         */
        //window.location.href = urlParams.get('redirect') || '/';
        history.push(urlParams.get('redirect') || '/');
        return;
      }else {
       //否则，把系统返回的错误信息给用户提示出来
       message.error(res.message);
      }
      //为了求稳，捕获一个异常
    } catch (error) {
      const defaultLoginFailureMessage = '登录失败，请重试！';
      console.log(error);
      message.error(defaultLoginFailureMessage);
    }
  };

  return (
    <div className={styles.container}>
      <Helmet>
        <title>登录 - 智能BI</title>
      </Helmet>
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <LoginForm
          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          logo={<img alt="logo" src="/logo.svg" />}
          title="智能 BI "
          subTitle={
          <a href = "https://github.com/iamxurulin" target = "_blank"
            rel="noreferrer">
            智能BI 是上海滩最具影响力的商业分析工具
            </a>
          }
          onFinish={async (values) => {
            await handleSubmit(values as API.UserLoginRequest);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'account',
                label: '账户密码登录',
              },
            ]}
          />

          {type === 'account' && (
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined />,
                }}
                placeholder={'请输入用户名'}
                rules={[
                  {
                    required: true,
                    message: '用户名是必填项！',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined />,
                }}
                placeholder={'请输入密码'}
                rules={[
                  {
                    required: true,
                    message: '密码是必填项！',
                  },
                ]}
              />
            </>
          )}

          <div
            style={{
              marginBottom: 24,
            }}
          >
            <Link to="/user/register">注册</Link>
          </div>
        </LoginForm>
      </div>
      <Footer />
    </div>
  );
};
export default Login;
