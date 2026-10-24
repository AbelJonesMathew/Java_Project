package com.campusvoice.dao;

import com.campusvoice.model.Comment;
import java.util.List;

public interface CommentDAO {
    boolean createComment(Comment comment);
    List<Comment> findByComplaintId(String complaintId);
}
