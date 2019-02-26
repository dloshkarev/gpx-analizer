$(document).ready(function () {
    /**
     * Create chart with performance statistic data
     * @param statistic performance statistic data
     */
    var createPerformanceChart = function (statistic) {
        if (statistic) {
            var labels = [];
            var data = [];
            statistic.forEach(function (item) {
                labels.push([
                    "Year: " + item.year,
                    "Week: " + item.week,
                    "Running index per week: " + item.avgRunningIndex
                ]);
                data.push(item.avgRunningIndex);
            });

            var ctx = $("#performanceChart");
            Chart.defaults.global.maintainAspectRatio = false;
            if (chart) {
                chart.config.data.labels = labels;
                chart.config.data.datasets[0].data = data;
                chart.update();
            } else {
                chart = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: labels,
                        datasets: [{
                            data: data,
                            backgroundColor: 'rgba(0,0,0,0.0)',
                            borderColor: "green"
                        }]
                    },
                    options: {
                        responsive: true,
                        legend: {
                            display: false
                        },
                        title: {
                            display: true,
                            text: 'Running index',
                            fontSize: 22
                        },
                        tooltips: {
                            mode: 'nearest',
                            intersect: false,
                            bodyFontSize: 14
                        },
                        scales: {
                            xAxes: [{
                                display: false,
                                scaleLabel: {
                                    display: true,
                                    fontSize: 18,
                                    labelString: 'Date'
                                }
                            }],
                            yAxes: [{
                                display: true,
                                scaleLabel: {
                                    display: true,
                                    fontSize: 18,
                                    labelString: 'Index'
                                }
                            }]
                        }
                    }
                });
            }
        }
    };

    var requestGuid = $('#requestGuidInput').val();
    $.ajax({
        url: '/rest/request/' + requestGuid + '/statistic',
        method: 'GET',
        dataType: 'JSON',
        success: function (statistic) {
            createPerformanceChart(statistic)
        }
    });
});