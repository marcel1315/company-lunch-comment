package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.dto.reply.CreateReplyDto;
import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.Reply;
import com.marceldev.companylunchcomment.exception.CommentsNotFoundException;
import com.marceldev.companylunchcomment.repository.CommentsRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.repository.ReplyRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReplyServiceTest {

  @Mock
  private ReplyRepository replyRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private CommentsRepository commentsRepository;

  @InjectMocks
  private ReplyService replyService;

  @Test
  @DisplayName("댓글 작성 - 성공")
  void create_reply() {
    //given
    CreateReplyDto dto = CreateReplyDto.builder()
        .content("댓글입니다.")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder().id(1L).build()
        ));
    when(commentsRepository.findById(any()))
        .thenReturn(Optional.of(
            Comments.builder().id(2L).build()
        ));

    //when
    replyService.createReply(1L, dto, "hello@example.com");
    ArgumentCaptor<Reply> captor = ArgumentCaptor.forClass(Reply.class);

    //then
    verify(replyRepository).save(captor.capture());
    assertEquals(1L, captor.getValue().getMember().getId());
    assertEquals(2L, captor.getValue().getComments().getId());
    assertEquals("댓글입니다.", captor.getValue().getContent());
  }

  @Test
  @DisplayName("댓글 작성 - 실패(코멘트가 존재하지 않음)")
  void create_reply_fail_no_comments() {
    //given
    CreateReplyDto dto = CreateReplyDto.builder()
        .content("댓글입니다.")
        .build();
    when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(
            Member.builder().id(1L).build()
        ));
    when(commentsRepository.findById(any()))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(CommentsNotFoundException.class,
        () -> replyService.createReply(1L, dto, "hello@example.com"));
  }
}