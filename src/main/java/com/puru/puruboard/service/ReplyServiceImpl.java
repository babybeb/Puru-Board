package com.puru.puruboard.service;

import com.puru.puruboard.domain.Post;
import com.puru.puruboard.domain.PostRepository;
import com.puru.puruboard.domain.Reply;
import com.puru.puruboard.domain.ReplyRepository;
import com.puru.puruboard.domain.UserRepository;
import com.puru.puruboard.dto.CreateReplyDto;
import com.puru.puruboard.dto.ReplyResponseDto;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ReplyServiceImpl implements ReplyService {
    
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    
    @Override
    public List<ReplyResponseDto> findAllByPostId(Long postId) {
        List<Reply> replyList = replyRepository.findAllByPostId(postId);
        
        return replyList.stream().filter(r -> !r.getIsDeleted())
            .map(ReplyResponseDto::new)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public Long createReply(Long postId, CreateReplyDto createReplyDto) {
        String nickname = userRepository.findByEmail(
            SecurityContextHolder.getContext().getAuthentication().getName()).get().getNickname();
        
        Optional<Post> post = postRepository.findById(postId);
        if (post.isEmpty()) {
            new Exception("존재하지 않는 게시물입니다.");
            return null;
        }
        
        Reply reply = Reply.builder()
            .author(nickname)
            .content(createReplyDto.getContent())
            .post(post.get())
            .build();
        
        return replyRepository.save(reply).getId();
    }
    
    @Override
    @Transactional
    public void deleteReply(Long replyId) {
        Optional<Reply> reply = replyRepository.findById(replyId);
        
        if (reply.isEmpty()) {
            new Exception("존재하지 않는 댓글입니다.");
            return;
        }
        
        // 현재 접속중인 유저의 닉네임
        String nickname = userRepository.findByEmail(
            SecurityContextHolder.getContext().getAuthentication().getName()).get().getNickname();
        if (!reply.get().getAuthor()
            .equals(nickname)) { // 현재 접속중인 유저의 닉네임과 댓글 작성자의 닉네임이 다르다면 예외 처리
            new Exception("유효하지 않은 접근입니다.");
            return;
        }
        
        reply.get().delete();
    }
    
    @Override
    @Transactional
    public Long updateReply(Long replyId, CreateReplyDto dto) {
        Optional<Reply> reply = replyRepository.findById(replyId);
        
        if (reply.isEmpty()) {
            new Exception("존재하지 않는 댓글입니다.");
            return null;
        }
        
        // 현재 접속중인 유저의 닉네임
        String nickname = userRepository.findByEmail(
            SecurityContextHolder.getContext().getAuthentication().getName()).get().getNickname();
        if (!reply.get().getAuthor()
            .equals(nickname)) { // 현재 접속중인 유저의 닉네임과 댓글 작성자의 닉네임이 다르다면 예외 처리
            new Exception("유효하지 않은 접근입니다.");
            return null;
        }
        
        reply.get().update(nickname, dto.getContent());
        return replyId;
    }
}