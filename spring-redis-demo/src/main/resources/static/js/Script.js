$(function() {
    $(".personalTimeline").click(function(){
        $(".globalTimelineDiv").css({"display" : "none"});
        $(".personalTimelineDiv").css({"display" : "block"});
        $(".personalTimeline").css({"background-color" : "#81a0fe"})
        $(".globalTimeline").css({"background-color" : "#3868fe"})
    });
    $(".globalTimeline").click(function(){
        $(".personalTimelineDiv").css({"display" : "none"});
        $(".globalTimelineDiv").css({"display" : "block"});
        $(".globalTimeline").css({"background-color" : "#81a0fe"})
        $(".personalTimeline").css({"background-color" : "#3868fe"})
    });
});

