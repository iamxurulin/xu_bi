import type { RequestOptions } from '@@/plugin/request/request';
import type { RequestConfig } from '@umijs/max';
import { message, notification } from 'antd';

enum ErrorShowType {
  SILENT = 0,
  WARN_MESSAGE = 1,
  ERROR_MESSAGE = 2,
  NOTIFICATION = 3,
  REDIRECT = 9,
}

export const errorConfig: RequestConfig = {
  errorConfig: {
    errorThrower: (res) => {
      const data = res as unknown as { code?: number; success?: boolean; data?: any; message?: string };
      if (data?.code !== 0 && data?.success !== true) {
        const error: any = new Error(data?.message || '请求失败');
        error.name = 'BizError';
        error.info = { errorCode: data?.code, errorMessage: data?.message, showType: ErrorShowType.ERROR_MESSAGE, data: data?.data };
        throw error;
      }
    },
    errorHandler: (error: any, opts: any) => {
      if (opts?.skipErrorHandler) throw error;
      if (error.name === 'BizError') {
        const errorInfo = error.info;
        if (errorInfo) {
          const { errorMessage, errorCode } = errorInfo;
          switch (errorInfo.showType) {
            case ErrorShowType.SILENT:
              break;
            case ErrorShowType.WARN_MESSAGE:
              message.warning(errorMessage);
              break;
            case ErrorShowType.ERROR_MESSAGE:
              message.error(errorMessage);
              break;
            case ErrorShowType.NOTIFICATION:
              notification.open({ description: errorMessage, message: errorCode });
              break;
            case ErrorShowType.REDIRECT:
              break;
            default:
              message.error(errorMessage);
          }
        }
      } else if (error.response) {
        message.error(`Response status:${error.response.status}`);
      } else if (error.request) {
        message.error('None response! Please retry.');
      } else {
        message.error('Request error, please retry.');
      }
    },
  },
  requestInterceptors: [
    (config: RequestOptions) => {
      return { ...config };
    },
  ],
  responseInterceptors: [
    (response) => {
      const data = response.data as unknown as { code?: number; success?: boolean; message?: string };
      if (data?.code !== 0 && data?.success !== true) {
        message.error(data?.message || '请求失败！');
      }
      return response;
    },
  ],
};