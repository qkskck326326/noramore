package com.develup.noramore.chatting.model.service;

import java.util.List;
import java.util.Map;

import com.develup.noramore.chatting.vo.ChattingRoom;
import com.develup.noramore.chatting.vo.Message;
import com.develup.noramore.member.model.vo.Member;


public interface ChattingService {
    
    List<ChattingRoom> selectRoomList(String string);

    int checkChattingNo(Map<String, Integer> map);

    int createChattingRoom(Map<String, Integer> map);

    int insertMessage(Message msg);

    int updateReadFlag(Map<String, Object> paramMap);

    List<Message> selectMessageList(Map<String, Object> paramMap);

   /** 채팅 상대 검색
    * @param map 
    * @return memberList
    */
   List<Member> selectTarget(Map<String, Object> map);

}