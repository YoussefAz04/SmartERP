import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { registerLicense } from '@syncfusion/ej2-base';

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));



registerLicense('Ngo9BigBOggjHTQxAR8/V1JEaF5cWWJCdkx0Qnxbf1x1ZFJMYllbRX5PIiBoS35Rc0VkW3dfeXdRRmlbUk1/VEFd');
//registerLicense("ORg4AjUWIQA/Gnt2VVhhQlFac11JW3xNYVF2R2FJe1RzdF9DZkwgOX1dQl9hSXtTcEVhWndceXFdQmY=");

