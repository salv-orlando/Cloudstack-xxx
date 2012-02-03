(function($, cloudStack) {
  cloudStack.uiCustom.dashboard = function() {
    /**
     * Retrieve chart data
     */
    var getData = function() {
      // Populate data
      $dashboard.find('[data-item]').hide();
      cloudStack.sections.dashboard[dashboardType].dataProvider({
        response: {
          success: function(args) {
            var $browser = $dashboard.closest('#browser .container');
            var data = args.data;

            // Iterate over data; populate corresponding DOM elements
            $.each(data, function(key, value) {
              var $elem = $dashboard.find('[data-item=' + key + ']');

              // This assumes an array of data
              if ($elem.is('ul')) {
                $elem.show();
                var $liTmpl = $elem.find('li').remove();
                $(value).each(function() {
                  var item = this;
                  var $li = $liTmpl.clone().appendTo($elem).hide();

                  if ($li.is('.zone-stats li')) {
                    $li.click(function() {
                      $browser.cloudBrowser('addPanel', {
                        title: 'Zone details',
                        parent: $dashboard.closest('.panel'),
                        maximizeIfSelected: true,
                        complete: function($newPanel) {
                          $newPanel.detailView($.extend(true, {},
                            cloudStack.sections.dashboard.admin.zoneDetailView,
                            {
                              $browser: $browser,
                              context: $.extend(true, {}, cloudStack.context, {
                                physicalResources: [{ id: item.zoneID }]
                              })
                            }
                          ));
                        }
                      })
                    });
                  }

                  $.each(item, function(arrayKey, arrayValue) {
                    var $arrayElem = $li.find('[data-list-item=' + arrayKey + ']');

                    $arrayElem.each(function() {
                      var $arrayElem = $(this);
                      
                      if ($arrayElem.hasClass('pie-chart')) {
                        // Generate pie chart
                        // -- values above 80 have a red color
                        pieChart($arrayElem, [
                          { data: [[1, 100 - arrayValue]], color: '#54697e' },
                          { data: [[1, arrayValue]], color: arrayValue < 80 ? 'orange' : 'red' }
                        ]);
                      } else {
                        if ($li.attr('concat-value')) {
                          $arrayElem.html(arrayValue.toString().split('<br/>').map(function(val) {
                            var concatValue = parseInt($li.attr('concat-value'));
                            
                            return val.length >= concatValue ? val.substring(0, concatValue).concat('...') : val;
                          }).join('<br/>'));
                        } else {
                          $arrayElem.html(arrayValue);
                        }

                        $arrayElem.attr('title', arrayValue.toString().replace('<br/>', ', '));
                      }
                    });
                  });

                  $li.attr({ title: item.description });

                  $li.fadeIn();
                });
              } else {
                $elem.each(function() {
                  var $item = $(this);
                  if ($item.hasClass('chart-line')) {
                    $item.show().animate({ width: value + '%' });
                  } else {
                    $item.hide().html(value).fadeIn();
                  }
                });
              }
            });
          }
        }
      });
    };

    /**
     * Render circular pie chart, without labels
     */
    var pieChart = function($container, data) {
      $container.css({ width: 70, height: 66 });
      $.plot($container, data, {
        width: 100,
        height: 100,
        series: {
          pie: {
            innerRadius: 0.7,
            show: true,
            label: {
              show: false
            }
          }
        },
        legend: {
          show: false
        }
      });
    };

    // Determine if user or admin dashboard should be shown
    var dashboardType = cloudStack.sections.dashboard.adminCheck({
      context: cloudStack.context
    }) ? 'admin' : 'user';

    // Get dashboard layout
    var $dashboard = $('#template').find('div.dashboard.' + dashboardType).clone();

    // View all action
    $dashboard.find('.view-all').click(function() {
      if ($(this).hasClass('network')) $('#navigation li.network').click();
      else $('#navigation li.events').click();
    });



    getData();

    return $dashboard;
  };
}(jQuery, cloudStack));
