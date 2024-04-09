package com.develup.noramore.commentqnaboard.model.dao;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.develup.noramore.commentqnaboard.model.vo.CommentQnaBoard;
import com.develup.noramore.commentrecrboard.model.vo.CommentRecrBoard;

@Repository("commentQnaBoardDao")
public class CommentQnaBoardDao {

	@Autowired
	SqlSessionTemplate sqlSessionTemplate;
	
	public int deleteComment(CommentQnaBoard commentQnaBoard) {
		// TODO Auto-generated method stub
		return sqlSessionTemplate.delete("qnacomment.deleteComment", commentQnaBoard);
	}

	public ArrayList<CommentQnaBoard> selectQnaComment(int boardId) {
		List<CommentQnaBoard> list = sqlSessionTemplate.selectList("qnacomment.selectQnaComment", boardId);
		return (ArrayList<CommentQnaBoard>)list;
	}

	public int updateComment(CommentQnaBoard commentQnaBoard) {
		return sqlSessionTemplate.update("qnacomment.updateComment", commentQnaBoard);
	}

	public int insertQnaComment(CommentQnaBoard commentQnaBoard) {
		// TODO Auto-generated method stub
		return sqlSessionTemplate.insert("qnacomment.insertQnaComment", commentQnaBoard);
	}

	public void deleteBoardComment(int boardId) {
		sqlSessionTemplate.delete("qnacomment.deleteBoardComment", boardId);
	}	
	
}
