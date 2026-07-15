import { request } from '@umijs/max';

export async function userRegister(params: API.UserRegisterRequest) {
  return request('/api/user/register', {
    method: 'POST',
    data: params,
    skipErrorHandler: true,
  });
}
