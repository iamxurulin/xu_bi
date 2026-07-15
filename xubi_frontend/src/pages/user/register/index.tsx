import { history, Link, useRequest } from '@umijs/max';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { LoginForm, ProFormText } from '@ant-design/pro-components';
import { App } from 'antd';
import type { FC } from 'react';
import { createStyles } from 'antd-style';
import { userRegister } from './service';

const useStyles = createStyles(({ token }) => ({
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    overflow: 'auto',
    backgroundImage:
      "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
    backgroundSize: '100% 100%',
  },
}));

const Register: FC = () => {
  const { styles } = useStyles();
  const { message } = App.useApp();

  const handleSubmit = async (values: API.UserRegisterRequest) => {
    try {
      const res = await userRegister(values);
      if (res?.code === 0) {
        message.success('注册成功！');
        history.push('/user/login');
      } else {
        message.error(res?.message || '注册失败，请重试');
      }
    } catch (err: any) {
      message.error(err?.message || '注册失败，请重试');
      console.error(err);
    }
  };

  return (
    <div className={styles.container}>
      <LoginForm
        contentStyle={{
          minWidth: 280,
          maxWidth: '75vw',
        }}
        logo={<img alt="logo" src="/logo.svg" />}
        title="智能 BI "
        subTitle="上海滩最具影响力的商业分析工具"
        submitter={{
          searchConfig: {
            submitText: '注册',
          },
        }}
        onFinish={async (values) => {
          await handleSubmit(values as API.UserRegisterRequest);
        }}
      >
        <ProFormText
          name="userAccount"
          fieldProps={{
            size: 'large',
            prefix: <UserOutlined />,
          }}
          placeholder={'请输入用户账号'}
          rules={[
            {
              required: true,
              message: '用户账号是必填项！',
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
            {
              min: 8,
              message: '密码不能少于8个字符',
            },
          ]}
        />
        <ProFormText.Password
          name="checkPassword"
          fieldProps={{
            size: 'large',
            prefix: <LockOutlined />,
          }}
          placeholder={'请确认密码'}
          rules={[
            {
              required: true,
              message: '确认密码是必填项！',
            },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('userPassword') === value) {
                  return Promise.resolve();
                }
                return Promise.reject(new Error('两次输入的密码不一致！'));
              },
            }),
          ]}
        />
        <div
          style={{
            marginBottom: 24,
          }}
        >
          <Link to="/user/login">使用已有账户登录</Link>
        </div>
      </LoginForm>
    </div>
  );
};
export default Register;