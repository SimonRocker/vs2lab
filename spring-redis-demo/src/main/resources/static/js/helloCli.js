var stompClient = null;

function connect() {
    var socket = new SockJS('/mySocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        /* setConnected(true);   --- nur für connect button notwendig*/
        console.log('Connected: ' + frame);
        /*
        hier alle Subscribes festlegen, wird dann vom Controller aus angesprochen
        im subscribe Funktionen ausführen, die in dieser Datei implementiert sind.

         */
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
        stompClient.subscribe('/topic/newPost', function (greeting) {
            // zweiter Subscriber
            var greetingContent = JSON.parse(greeting.body).content;
            var newPostData = JSON.parse(greetingContent);
            showNewPostInTimeline(newPostData.newPostText, newPostData.currentUsername, newPostData.date);
        });
    });

}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
}

function sendNewPost() {
    var newPostText = document.getElementById('newPostText').value;
    var currentUsername = document.getElementById('submitNewPost').name;
    var date = document.getElementById('submitNewPost').value


    debugger;

    stompClient.send("/app/newPost", {}, JSON.stringify({
        /* es muss 'name' da stehen*/
        'name' : JSON.stringify({
            'newPostText' : newPostText,
            'currentUsername' : currentUsername,
            'date' : date
        })

    }));
}

function showNewPostInTimeline(message, user, date) {
    debugger;

    var addToTimeline = "<ul th:each=\"element : ${posts}\">\n" +
        "                    <li>\n" +
        "                        <div class=\"post\">\n" +
        "                            <div class=\"postHeader\">\n" +
        "                                <!--<img class=\"profilePic\"  href=\"../css/profileIcon.png\" th:src=\"@{/css/profileIcon.png}\"/>-->\n" +
        "                                <div class=\"profilePic glyphicon glyphicon-user\"></div>\n" +
        "                                <h2 class=\"author\" th:text=\"${element.value.getUsername()}\">" + user + "</h2>\n" +
        "                            </div>\n" +
        "\n" +
        "                            <p class=\"content\" th:text=\"${element.value.getText()}\">" + message + "</p>\n" +
        "                            <P class=\"date\" th:text=\"${element.value.getDate()}\">" + date + "</P>\n" +
        "                        </div>\n" +
        "                    </li>\n" +
        "                </ul>";

    $('#globalTimeline').prepend(addToTimeline);


    var jquerySelector = '#followingRight:contains(' + user + ')'
    if ($(jquerySelector).length > 0) {
        $('#personalTimeLine').prepend(addToTimeline);
    }



    var pushMessage = document.getElementById('pushMessage');
    var p2 = document.createElement('p');
    p2.appendChild(document.createTextNode('there is a new post!'));
    pushMessage.appendChild(p2);

}




/*
______________________________________________________________________________________________
BEISPIEL FOLIEN
 */

function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('conversationDiv').style.visibility = connected ? 'visible' : 'hidden';
    document.getElementById('response').innerHTML = '';
}

function sendName() {
    var name = document.getElementById('name').value;
    stompClient.send("/app/hello", {}, JSON.stringify({
        'name' : name
    }));
}

function showGreeting(message) {
    var response = document.getElementById('response');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(message));
    response.appendChild(p);
}

