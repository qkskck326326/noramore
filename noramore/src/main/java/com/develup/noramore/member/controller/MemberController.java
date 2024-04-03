package com.develup.noramore.member.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.develup.noramore.member.model.service.MemberService;
import com.develup.noramore.member.model.vo.Member;

@Controller
public class MemberController {
	// 이 클래스 안에 메소드들이 동작이 잘 되는지 또는 전달값이나 리턴값을 확인하기 위한 용도로 로그객체를 생성함
	private static final Logger logger = LoggerFactory.getLogger(MemberController.class); // 다른 클래스를 통해서 얻어냄,
																							// org.slf4j.Logger로
																							// import하기

	@Autowired
	private MemberService memberService;

	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder; // spring-security.xml에서의 id를 가져다 변수 명으로 사용
	// 암호를 암호화 함.

	// 뷰 페이지 내보내기용 메소드 ---------------------------------------------------
	@RequestMapping(value = "moveLoginPage.do", method = { RequestMethod.GET, RequestMethod.POST })
	// RequestMethod.GET : get방식으로 전송오면 받음, RequestMethod.POST : post방식으로 전송오면 받음
	public String moveLoginPage() {
		return "/member/moveLoginPage";

	}

	@RequestMapping(value = "enrollPage.do", method = { RequestMethod.GET, RequestMethod.POST })
	// RequestMethod.GET : get방식으로 전송오면 받음, RequestMethod.POST : post방식으로 전송오면 받음
	public String enrollPage() {
		return "/member/moveEnrollPage";

	}

	@RequestMapping(value = "findIDPage.do", method = { RequestMethod.GET, RequestMethod.POST })
	// RequestMethod.GET : get방식으로 전송오면 받음, RequestMethod.POST : post방식으로 전송오면 받음
	public String findIDPage() {
		return "/member/findIDPage";

	}

	@RequestMapping(value = "findPWPage.do", method = { RequestMethod.GET, RequestMethod.POST })
	// RequestMethod.GET : get방식으로 전송오면 받음, RequestMethod.POST : post방식으로 전송오면 받음
	public String findPWPage() {
		return "/member/findPWPage";

	}
	
	
	@RequestMapping(value = "findPW2.do", method = { RequestMethod.GET, RequestMethod.POST })
	// RequestMethod.GET : get방식으로 전송오면 받음, RequestMethod.POST : post방식으로 전송오면 받음
	public String findPWPage2() {
		return "/member/findPWPage2";

	}
	
	
	

	@RequestMapping(value = "my.do", method = { RequestMethod.GET, RequestMethod.POST })
	// RequestMethod.GET : get방식으로 전송오면 받음, RequestMethod.POST : post방식으로 전송오면 받음
	public String mypage() {
		return "/member/mypage";

	}

	// 로그인 처리용
	@RequestMapping(value = "login.do", method = RequestMethod.POST)
	public String loginMethod(Member member, 
								HttpSession session, 
								SessionStatus status, 
								Model model,
								RedirectAttributes ra) { // HttpSession : 세션 자동 생성, SessionStatus : 세션 상태 파악
																										
		logger.info("login.do : " + member.toString());

		Member loginMember = memberService.selectMember(member.getMemberID());

		if (loginMember != null
				&& this.bcryptPasswordEncoder.matches(member.getMemberPWD(), loginMember.getMemberPWD())) {
			session.setAttribute("loginMember", loginMember); // 세션이름에 loginMember을 저장함
			status.setComplete(); // 로그인 성공 요청결과로 HttpStatus code 200 보냄
			return "common/home";
		} else {
			return "redirect:moveLoginPage.do";
		}
	}

	// 로그아웃 처리용 메소드

	@RequestMapping("logout.do")
	public String logoutMethod(HttpServletRequest request, Model model) {
		HttpSession session = request.getSession(false);
		// 세션 객체가 있으면 리턴받고, 없으면 null 리턴
		if (session != null) {
			session.invalidate();
			return "common/home";
		} else {
			model.addAttribute("message", "로그인 세션이 존재하지 않습니다.");
			return "redirect:moveLoginPage.do";
		}
	}

	// 회원 가입 요청 처리용 메소드
	@RequestMapping(value = "enroll.do", method = RequestMethod.POST)
	public String memberInsertMethod(Member member,
			/* @RequestParam("email2") String email2, */
			@RequestParam("road") String road, @RequestParam("street") String street,
			@RequestParam("detail") String detail, @RequestParam("ref") String ref, Model model) throws Exception {
		logger.info("enroll.do : " + member);

		// 패스워드 암호화 처리
		member.setMemberPWD(bcryptPasswordEncoder.encode(member.getMemberPWD()));
		logger.info("after encode : " + member.getMemberPWD());
		logger.info("pwd length : " + member.getMemberPWD().length());

		// 주소 합치기
		if (road != null && street != null && detail != null) {
			String addressAdd = road + street + detail + ref;
			member.setAddress(addressAdd);
		}
		
		int dup = memberService.selectCheckId(member.getMemberID());
		
	
		if (memberService.insertMember(member) > 0 && dup == 0 ) {
			return "member/moveLoginPage";
		} else {
			model.addAttribute("message", "회원 가입 실패! 확인하고 다시 가입해 주세요.");
			return "redirect:moveEnrollPage.do";
		}
	}

	
	
	
	// ajax 통신으로 가입할 아이디 중복 확인 요청 처리용 메소드
	@RequestMapping(value = "idchk.do", method = RequestMethod.POST)
	public void dupCheckId(@RequestParam("memberID") String memberid, HttpServletResponse response) throws IOException {

		int idCount = memberService.selectCheckId(memberid);

		String returnStr = null;
		if (idCount == 0) {
			returnStr = "ok";
		} else {
			returnStr = "dup";
		}

		// response 를 이용해서 클라이언트와 출력스트림을 열어서 문자열값 내보냄
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		out.append(returnStr);
		out.flush();
		out.close();
	}

	
	
	
	// ajax 통신으로 가입할 이메일 중복 확인 요청 처리용 메소드
	@RequestMapping(value = "emailchk.do", method = RequestMethod.POST)
	public void dupCheckEmail(@RequestParam("email") String email, HttpServletResponse response) throws IOException {

		int idCount = memberService.selectCheckEmail(email);

		String returnStr = null;
		if (idCount == 0) {
			returnStr = "ok";
		} else {
			returnStr = "dup";
		}

		// response 를 이용해서 클라이언트와 출력스트림을 열어서 문자열값 내보냄
		response.setContentType("text/html; charset=utf-8");
		PrintWriter out = response.getWriter();
		out.append(returnStr);
		out.flush();
		out.close();
	}

	// 아이디 찾기에서 이름, 이메일 같은회원 > 아이디확인 페이지로 감
	@RequestMapping(value = "emailIdChk.do", method = RequestMethod.POST)
	public String emailIdChk(Member member, Model model) throws Exception {
		logger.info("emailIdChk.do : " + member);
		
		Member member2 = memberService.selectFindId(member);
		
		if ( member2 != null) {
			model.addAttribute("member2",member2);
			logger.info("member2 : " + member2.getMemberID());
			return "member/idChkPage";
		} else {
			model.addAttribute("message", "없는 회원입니다. 다시 확인해 주세요!");
			return "member/findIDPage";
		}
	}
	
	
	@RequestMapping(value = "emailIdChk2.do", method = RequestMethod.POST)
	public String emailIdChk2(Member member, Model model) throws Exception {
		logger.info("emailIdChk.do : " + member);
		
		Member member2 = memberService.selectFindId(member);
		
		if ( member2 != null) {
			model.addAttribute("member2",member2);
			logger.info("member2 : " + member2.getMemberID());
			return "member/pwUpdatePage";
		} else {
			model.addAttribute("message", "없는 회원입니다. 다시 확인해 주세요!");
			return "member/findPWPage2";
		}
	}

	
//	@RequestMapping(value = "bringId.do", method = RequestMethod.POST)
//	public void bringIdMethod(@RequestParam("email") String email, HttpServletResponse response) throws IOException {
//
//		int idCount = memberService.selectCheckEmail(email);
//
//		String returnStr = null;
//		if (idCount == 0) {
//			returnStr = "ok";
//		} else {
//			returnStr = "dup";
//		}
//
//		// response 를 이용해서 클라이언트와 출력스트림을 열어서 문자열값 내보냄
//		response.setContentType("text/html; charset=utf-8");
//		PrintWriter out = response.getWriter();
//		out.append(returnStr);
//		out.flush();
//		out.close();
//	}
	
	
	
	// ajax 통신으로 찾을 비밀번호의 아이디 확인 요청 처리용 메소드
		@RequestMapping(value = "dupIdCheck.do", method = RequestMethod.POST)
		public void dupIdCheck(@RequestParam("memberID") String memberid, HttpServletResponse response) throws IOException {

			int idCount = memberService.selectCheckId(memberid);

			String returnStr = null;
			if (idCount == 0) {
				returnStr = "ok";
			} else {
				returnStr = "dup";
			}

			// response 를 이용해서 클라이언트와 출력스트림을 열어서 문자열값 내보냄
			response.setContentType("text/html; charset=utf-8");
			PrintWriter out = response.getWriter();
			out.append(returnStr);
			out.flush();
			out.close();
		}
	
		
		//비밀번호 재설정
		@RequestMapping(value = "pwChange.do", method = RequestMethod.POST)
		public String pwChange(Member member, Model model) throws Exception {
			logger.info("emailIdChk.do : " + member);
			
			Member origin = memberService.selectOriginPw(member.getMemberID());
			String originPw = origin.getMemberPWD();
			
			// 새로운 암호가 전송이 왔다면, 패스워드 암호화 처리함
			String memberPwd = member.getMemberPWD().trim(); // 공백을 없앰
			logger.info("새로운 암호 : " + memberPwd + ", " + memberPwd.length());
			if (memberPwd != null && memberPwd.length() > 0) { // 암호가 전송이 왔다면
				// 암호화된 기존의 패스워드와 새로운 패스워드를 비교해서 다른 값이면
				if (this.bcryptPasswordEncoder.matches(memberPwd, originPw)) { // 입력한 패스워드, 암호화된 값 가져온것
					// member 에 새로운 패스워드를 암호화해서 저장함
					member.setMemberPWD(this.bcryptPasswordEncoder.encode(memberPwd));
				}
			} else { // 새로운 암호가 null 또는 글자갯수가 0일때는
				// 기존 암호이면, 원래 암호화된 패스워드를 저장함
				member.setMemberPWD(originPw);
			}
			
			if ( memberService.updatePw(member) > 0) {

				return "member/moveLoginPage";
			} else {
				model.addAttribute("message", "없는 회원입니다. 다시 확인해 주세요!");
				return "member/pwUpdatePage";
			}
		}
		
	
	
	
	@Autowired
	JavaMailSenderImpl mailSender;

	// 이메일 인증
	@PostMapping("emailAuth.do")
	@ResponseBody
	public String emailAuth(String email) {

		logger.info("전달 받은 이메일 주소 : " + email);

		// 난수의 범위 111111 ~ 999999 (6자리 난수)
		Random random = new Random();
		int checkNum = random.nextInt(888888) + 111111;

		// 이메일 보낼 양식
		String setFrom = "noramore12@naver.com"; // 2단계 인증 x, 메일 설정에서 POP/IMAP 사용 설정에서 POP/SMTP 사용함으로 설정o
		String toMail = email;
		String title = "회원가입 인증 이메일 입니다.";
		String content = "인증 코드는 " + checkNum + " 입니다." + "<br>" + "해당 인증 코드를 인증 코드 확인란에 기입하여 주세요.";

		System.out.println("메일 내용 : " + content + "보낸에메일 : " + setFrom);
		try {
			MimeMessage message = mailSender.createMimeMessage(); // Spring에서 제공하는 mail API
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(setFrom);
			helper.setTo(toMail);
			helper.setSubject(title);
			helper.setText(content, true);
			mailSender.send(message);
			logger.info("랜덤숫자!!! : " + checkNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray jarr = new JSONArray();
		JSONObject checkObject = new JSONObject();
		checkObject.put("code", checkNum);

		logger.info("랜덤숫자 : " + checkNum);
		return checkObject.toJSONString();
	}

	// 이메일 인증 >> 아이디 찾기
	@PostMapping("emailAuth2.do")
	@ResponseBody
	public String emailAuth2(String email) {

		logger.info("전달 받은 이메일 주소 : " + email);

		// 난수의 범위 111111 ~ 999999 (6자리 난수)
		Random random = new Random();
		int checkNum = random.nextInt(888888) + 111111;

		// 이메일 보낼 양식
		String setFrom = "noramore12@naver.com"; // 2단계 인증 x, 메일 설정에서 POP/IMAP 사용 설정에서 POP/SMTP 사용함으로 설정o
		String toMail = email;
		String title = "회원가입 인증 이메일 입니다.";
		String content = "인증 코드는 " + checkNum + " 입니다." + "<br>" + "해당 인증 코드를 인증 코드 확인란에 기입하여 주세요.";

		System.out.println("메일 내용 : " + content + "보낸에메일 : " + setFrom);
		try {
			MimeMessage message = mailSender.createMimeMessage(); // Spring에서 제공하는 mail API
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
			helper.setFrom(setFrom);
			helper.setTo(toMail);
			helper.setSubject(title);
			helper.setText(content, true);
			mailSender.send(message);
			logger.info("랜덤숫자!!! : " + checkNum);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JSONArray jarr = new JSONArray();
		JSONObject checkObject = new JSONObject();
		checkObject.put("code", checkNum);

		logger.info("랜덤숫자 : " + checkNum);
		return checkObject.toJSONString();
	}

}
