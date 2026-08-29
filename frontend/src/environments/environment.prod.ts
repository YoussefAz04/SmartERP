import { runtimeApiUrl } from './runtime-config';

export const environment = {
  production: true,
  apiUrl: runtimeApiUrl('http://localhost:8020')
};
