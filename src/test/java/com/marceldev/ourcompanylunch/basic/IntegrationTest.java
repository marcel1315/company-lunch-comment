package com.marceldev.ourcompanylunch.basic;

import com.marceldev.ourcompanylunch.component.EmailSender;
import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.repository.comment.CommentRepository;
import com.marceldev.ourcompanylunch.repository.company.CompanyRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerImageRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerSubscriptionRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.verification.VerificationRepository;
import com.marceldev.ourcompanylunch.security.WithCustomUser;
import com.marceldev.ourcompanylunch.service.CommentService;
import com.marceldev.ourcompanylunch.service.CompanyService;
import com.marceldev.ourcompanylunch.service.DinerImageService;
import com.marceldev.ourcompanylunch.service.DinerService;
import com.marceldev.ourcompanylunch.service.DinerSubscribeService;
import com.marceldev.ourcompanylunch.service.DinerTagService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@WithCustomUser(username = "jack@example.com")
public abstract class IntegrationTest {

  // --- Repository ---

  @Autowired
  protected CompanyRepository companyRepository;

  @Autowired
  protected MemberRepository memberRepository;

  @Autowired
  protected DinerRepository dinerRepository;

  @Autowired
  protected DinerImageRepository dinerImageRepository;

  @Autowired
  protected DinerSubscriptionRepository dinerSubscriptionRepository;

  @Autowired
  protected VerificationRepository verificationRepository;

  @Autowired
  protected CommentRepository commentRepository;

  // --- Service ---

  @Autowired
  protected CommentService commentService;

  @Autowired
  protected CompanyService companyService;

  @Autowired
  protected DinerService dinerService;

  @Autowired
  protected DinerImageService dinerImageService;

  @Autowired
  protected DinerTagService dinerTagService;

  @Autowired
  protected DinerSubscribeService dinerSubscribeService;

  // --- Mock ---

  @MockBean
  protected EmailSender emailSender;

  @MockBean
  protected S3Manager s3Manager;

  // --- Etc ---

  @PersistenceContext
  protected EntityManager entityManager;

}
