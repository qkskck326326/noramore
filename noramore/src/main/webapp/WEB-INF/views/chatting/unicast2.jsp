<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"  %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Unicast</title>
<script type="text/javascript" src="resources/js/jquery-3.7.0.min.js"></script>
<style>

.container {
    position: relative;
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    margin: auto; /* 중앙 정렬 */
}

#messageWindow {
    background: LightSkyBlue;
    height: 300px;
    overflow: auto;
}

.chat_content {
    background: rgb(255, 255, 102);
    padding: 10px;
    border-radius: 10px;
    display: inline-block;
    position: relative;
    margin: 10px;
    float: right;
    clear: both;
}

.chat_content:after {
    content: '';
    position: absolute;
    right: 0;
    top: 50%;
    width: 0;
    height: 0;
    border: 20px solid transparent;
    border-left-color: rgb(255, 255, 102);
    border-right: 0;
    border-top: 0;
    margin-top: -3.5px;
    margin-right: -10px;
}

.other-side {
    background: white;
    float: left;
    clear: both;
}

.other-side:after {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    width: 0;
    height: 0;
    border: 20px solid transparent;
    border-right-color: white;
    border-left: 0;
    border-top: 0;
    margin-top: -3.5px;
    margin-left: -10px;
}
</style>
<c:import url="/WEB-INF/views/common/header.jsp" />
<c:import url="/WEB-INF/views/common/sideSample.jsp" />
</head>
<body>
<div class="container">
    <h2>채팅</h2>
    <input type="hidden" id="chat_id" value="${ loginMember.memberID }"/> <br> 
    상대방 ID : <input type="text" id="recvUser" size="12"/> &nbsp;
    <button type="button" id="startBtn">채팅하기</button><br>
    
    <!-- 채팅 창 구현 부분 -->
    
    <div style="display:none;" id="chatbox">
        <fieldset>
            <div id="messageWindow"></div><br>
            <input type="text" id="inputMessage" onkeyup="enterKey()"/>
            <input type="submit" value="보내기" onclick="send()">
            <button type="button" id="endBtn">나가기</button>
        </fieldset>
    </div>
</div>

<script>
$('#startBtn').on('click',function(){
    $('#chatbox').css('display', 'block');
    $(this).css('display', 'none');
    connection();
});

$('#endBtn').on('click',function(){
    $('#chatbox').css('display', 'none');
    $('#startBtn').css('display', 'inline');
    webSocket.send($('#chat_id').val()
            +"|님이 채팅방을 퇴장하였습니다.");
    webSocket.close();
});

// 채팅창 내용 부분
var $textarea = $('#messageWindow');

// 채팅 서버
var webSocket = null;

// 내가 보낼 문자열을 담은 input 태그
var $inputMessage = $('#inputMessage');

function connection(){
    webSocket = new WebSocket('ws://localhost:8080'+
    '<%=request.getContextPath()%>/chat');
    
    // 웹 소켓을 통해 연결이 이루어 질 때 동작할 메소드
    webSocket.onopen = function(event){
        
		$textarea.html("<p class='chat_content'>"
                +$('#chat_id').val()+"님이 입장하셨습니다.</p><br>");
        
        // 웹 소켓을 통해 만든 채팅 서버에 참여한 내용을
        // 메시지로 전달
        // 내가 보낼 때에는 send / 서버로부터 받을 때에는 message
        webSocket.send($('#chat_id').val()+"|님이 입장하셨습니다.");
        
        $.ajax({
        	url:"removeRequest.do"
        	type: "post"
        	data: {	sender : $('#chat_id').val(),
        			receiver : $('#recvUser').val()}
        });
        
    };
    
    // 서버로부터 메시지를 전달 받을 때 동작하는 메소드
    webSocket.onmessage = function(event){
        onMessage(event);
    }
    
    // 서버에서 에러가 발생할 경우 동작할 메소드
    webSocket.onerror = function(event){
        onError(event);
         console.log('닫힘 코드:', event.code);
    }
    
    // 서버와의 연결이 종료될 경우 동작하는 메소드
    webSocket.onclose = function(event){
        //onClose(event);
         console.log('닫힘 코드:', event.code);
    }
}

// 엔터키를 누를 경우 메세지 보내기
function enterKey(){
    if(window.event.keyCode == 13)
        send();
}

// 서버로 메시지를 전달하는 메소드
function send(){
    if ($inputMessage.val() == ""){
        // 메시지를 입력하지 않을 경우
        alert("메시지를 입력해 주세요!");
    } else {
        // 메시지가 입력 되었을 경우
        $textarea.html(
            $textarea.html()
            +"<p class='chat_content'>"
            +$inputMessage.val()+"</p><br>");
        
        webSocket.send($('#chat_id').val()+"|"+$inputMessage.val());
        
        $inputMessage.val("");
       

        
    }
    
    $textarea.scrollTop($textarea.height());
}

// 서버로부터 메시지를 받을 때 수행할 메소드
function onMessage(event) {
    var message = event.data.split("|");
    
    // 보낸 사람의 ID
    var sender = message[0];
    
    // 전달한 내용
    var content = message[1];
    
    if(content == "" || !sender.match($('#recvUser').val())){
        // 전달 받은 글이 없거나, 전달한 사람이
        // 내가 연결하려는 상대방이 아닐 경우
        // 아무 내용도 실행하지 않겠다.
    } else {
        $textarea.html(
            $textarea.html()
           +"<p class='chat_content other-side'>"
           +sender+" : "
           +content
           +"</p><br>");
        
        $textarea.scrollTop($textarea.height());
    }
}

function onError(event) {
    alert(event.data);
}

function onClose(event) {
    alert(event);
}

</script>

</body>
</html>
