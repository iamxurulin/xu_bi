import type { ProLayoutProps } from '@ant-design/pro-components';

/**
 * @name
 */
const Settings: ProLayoutProps & {
  pwa?: boolean;
  logo?: string;
} = {
  "navTheme": "light",
  "layout": "top",
  "contentWidth": "Fixed",
  "fixedHeader": true,
  "fixSiderbar": false,
  "colorPrimary": "#1677FF",
  "splitMenus": false

};

export default Settings;
