var chart;
var yearStartValues = null;
var yearFinishValues = null;

/**
 * Create chart with data
 * @param activities activities data
 */
var createActivitiesChart = function (activities) {
    if (activities) {
        var labels = [];
        var data = [];
        activities.forEach(function (item) {
            labels.push([
                "Date: " + item.eventDate,
                "Distance: " + item.distance,
                "Duration: " + item.duration,
                "Avg pace: " + item.avgPace,
                "Avg HR: " + item.avgHr,
                "Elevation gain: " + item.elevationGain
            ]);
            data.push(item.runningIndex);
        });

        var ctx = $("#activitiesChart");
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
                        borderColor: "red"
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

var buildActivitiesChart = function () {
    var requestGuid = $('#requestGuidInput').val();
    $.ajax({
        url: '/rest/request/' + requestGuid,
        method: 'GET',
        dataType: 'JSON',
        data: {
            yearStart: $('#yearStart').val(),
            yearFinish: $('#yearFinish').val()
        },
        success: function (activities) {
            createActivitiesChart(activities)
        }
    });
};

/**
 * Show button click event
 */
$('#chartButton').click(function (event) {
    buildActivitiesChart();
});

/**
 * Change year start event
 */
$("#yearStart").change(function () {
    var yearStart = $(this).val();
    var yearFinishSelect = $("#yearFinish");

    if (!yearFinishValues) {
        yearFinishValues = [];
        $("#yearFinish option").each(function () {
            yearFinishValues.push($(this).val());
        });
    }

    yearFinishSelect.empty();
    $.each(yearFinishValues, function(n, year) {
        if (year >= yearStart) {
            yearFinishSelect.append($("<option></option>").val(year).text(year));
        }
    });
});

/**
 * Change year finish event
 */
$("#yearFinish").change(function () {
    var yearFinish = $(this).val();
    var yearStartSelect = $("#yearStart");

    if (!yearStartValues) {
        yearStartValues = [];
        $("#yearStart option").each(function () {
            yearStartValues.push($(this).val());
        });
    }

    yearStartSelect.empty();
    $.each(yearStartValues, function(n, year) {
        if (year <= yearFinish) {
            yearStartSelect.append($("<option></option>").val(year).text(year));
        }
    });
});