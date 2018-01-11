import { PRO4WebAppPage } from './app.po';

describe('pro4-web-app App', () => {
  let page: PRO4WebAppPage;

  beforeEach(() => {
    page = new PRO4WebAppPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
