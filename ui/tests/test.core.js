(function($) {
  var $cloudStack, cloudStack;

  module('Core widget', {
    setup: function() {
      cloudStack = {
        sections: {
          home: {
            show: function() { return $('<div>').addClass('test123'); }
          },
          sectionA: {},
          sectionB: {},
          sectionC: {}
        },

        home: 'home' 
      };     

      $cloudStack = $('<div>');
      ok($cloudStack.cloudStack(cloudStack), 'Basic widget initialized');
    }
  });

  test('Container/wrappers', function() {
    equal($cloudStack.find('[cloudStack-container]').size(), 1, 'Main sub-container present');
    equal($cloudStack.find('#main-area').size(), 1, 'Main area present');
  });

  test('Header', function() {
    var $header = $cloudStack.find('#header');
    var $userOptions = $cloudStack.find('#user-options');
    var $notifications = $header.find('.button.notifications');
    var $notificationTotal = $notifications.find('.total span');
    var $viewSwitcher = $header.find('.button.view-switcher');

    equal($header.size(), 1, 'Header present');
    equal($userOptions.size(), 1, 'User options present');
    equal($userOptions.find('a').size(), 2, 'User options has correct # of options');
    equal($notifications.size(), 1, 'Notifications present');
    equal($notificationTotal.html(), '0', 'Notifications initialized properly');
    equal($viewSwitcher.size(), 1, 'View switcher present'); 
  });

  test('Navigation', function() {
    var $navigation = $cloudStack.find('#navigation');

    equal($navigation.size(), 1, 'Navigation present');
    equal($navigation.find('li').size(), 4, 'Navigation has correct # of nav items');
  });

  test('Browser / page generation', function() {
    var $browser = $cloudStack.find('#browser');
    var $browserContainer = $browser.find('.container');
    var $homePage = $browserContainer.find('.panel div.test123');
    var $breadcrumbs = $browser.find('#breadcrumbs li');
    var $homeBreadcrumb = $browser.find('#breadcrumbs .home');

    equal($browser.size(), 1, 'Browser intialized');
    equal($homePage.size(), 1, 'Home page is visible');
    equal($breadcrumbs.size(), 0, 'No initial breadcrumbs');
    equal($homeBreadcrumb.size(), 1, 'Home breadcrumb active');
  });
}(jQuery));
