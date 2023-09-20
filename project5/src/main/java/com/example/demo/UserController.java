package com.example.demo;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.models.Role;
import com.example.demo.models.User;
import com.example.demo.service.ProductService;
import com.example.demo.service.RoleService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	JavaMailSender javaMailSender;
	@Autowired
	UserService userService;
	@Autowired
	ProductService productService;
	@Autowired
	RoleService roleService;

	@RequestMapping("/")
	public String home() {
		return "index";
	}

	@RequestMapping("/login")
	public String Login() {
		return "Login";
	}

	// load lên danh mục loại tk
	@ModelAttribute(name = "ROLE")
	public List<Role> getAllRole() {
		return roleService.findAll();
	}

	// kiểm tra login
	@PostMapping("/checklogin")
	public String checkLogin(@RequestParam("username") String username, @RequestParam("password") String password,
			ModelMap map, HttpSession session) {
		User user = userService.checkLogin(username, password);
		if (user != null) {
			session.setAttribute("USERNAME", username);
			return "redirect:/product/productList";
		} else {
			map.addAttribute("ERROR", "	Username and password not exist");
		}
		return "Login";
	}

	// tạo acc
	private int randomNumber;

	public void test() {
		Random random = new Random();
		randomNumber = random.nextInt(9000) + 1000;
	}

	@RequestMapping("/CreateAccount")
	public String CreateAccount(ModelMap map) {
		User u = new User();
		map.addAttribute("USER", u);
		map.addAttribute("ACTION", "/user/checkCreateAccount");
		test();
		return "Login_CreateAccount";
	}

	// validate
	@RequestMapping("/checkCreateAccount")
	public String checkCreateAccount(@ModelAttribute("USER") User user, ModelMap map) {
		Optional<User> u = userService.findByUserName(user.getUsername());
		Optional<User> u2 = userService.findByEmail(user.getEmail());
		if (u.isPresent()) { // đã tồn tại
			map.addAttribute("ERROR", "	Trùng username ");
			return "Login_CreateAccount";

		}
		if (u2.isPresent()) { // đã tồn tại
			map.addAttribute("ERROR", "	Trùng email ");
			return "Login_CreateAccount";
		} else

			return sendMail(map, user);
	}

	private String to;
	private User user;
	private int dem = 0;
	private volatile boolean stopCountdown = false;

//gửi mã otp
	@RequestMapping("/sendCode")
	public String sendMail(ModelMap map, @ModelAttribute("USER") User user) {
		SimpleMailMessage msg = new SimpleMailMessage();
		test();
		msg.setTo(user.getEmail());
		msg.setSubject("CREATE ACCOUNT");
		msg.setText("MÃ OTP: " + randomNumber);
		javaMailSender.send(msg);
		stopCountdown = false;

		Thread countdownThread = new Thread(() -> {
			long startTime = System.currentTimeMillis();
			int seconds = 0;
			while (seconds < 30) {
				long currentTime = System.currentTimeMillis();
				seconds = (int) ((currentTime - startTime) / 1000);
			}
			if (!stopCountdown) {
				System.out.println("Đã đếm được 60 giây!");
				dem = 1;
				this.dem = dem;
			}
		});
		countdownThread.start();
		this.user = user;
		return "Login_Create_Result";
	}

	// check mã
	@RequestMapping("/checkma")
	public String checkMa(ModelMap map, @RequestParam("OTP") int OTP) {
		System.out.println(dem);
		System.out.println(randomNumber);
		System.out.println(OTP);
		if (dem == 1) {
			map.addAttribute("ERROR", "	Mã hết hạn, yêu cầu thao tác lại");
			/////////////////////////////////
			/// mới viết thêm
			this.dem = 0;
			stopCountdown = true;
			return "redirect:/user/CreateAccount";
		}
		if (randomNumber == OTP) {
			userService.save(user);
			/////////////////////////////////
			/// mới viết thêm
			this.dem = 0;
			this.user = null;
			stopCountdown = true;
			return "redirect:/user/login";
		} else {
			this.dem = 0;
			stopCountdown = true;
			System.out.println("fail");
			return "redirect:/user/CreateAccount";
		}

	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute("USERNAME");
		return "Login";
	}

	// quên mk
	@RequestMapping("/ForgotPassword")
	public String ForgotPassword() {
		return "Login_ForgotPassword";
	}

	private int randomNumber2;

	public void test2() {
		Random random = new Random();
		randomNumber2 = random.nextInt(9000) + 1000;
	}

	private int dem2 = 0;
	private String passwordNew;
	private String to2;
	private volatile boolean stopCountdown2 = false;

	@RequestMapping("/sendOTPForgotPassword")
	public String sendOTPForgotPassword(ModelMap map, @RequestParam("to2") String to2,
			@RequestParam("passwordNew") String passwordNew) {
		SimpleMailMessage msg = new SimpleMailMessage();
		test2();
		msg.setTo(to2);
		msg.setSubject("ForgotPassword");
		msg.setText("MÃ OTP: " + randomNumber2);
		javaMailSender.send(msg);
		stopCountdown2 = false;

		Thread countdownThread = new Thread(() -> {
			long startTime = System.currentTimeMillis();
			int seconds = 0;
			while (seconds < 30) {
				long currentTime = System.currentTimeMillis();
				seconds = (int) ((currentTime - startTime) / 1000);
			}
			if (!stopCountdown2) {
				System.out.println("Đã đếm được 60 giây!");
				dem2 = 1;
				this.dem2 = dem2;
			}
		});
		countdownThread.start();
		this.to2 = to2;
		this.passwordNew = passwordNew;
		System.out.println(passwordNew);
		return "Login_ForgotPassword_Result";

	}

	@RequestMapping("/checkmaForgotPassword")
	public String checkMaForgotPassword(ModelMap map, @RequestParam("OTPForgotPassword") int OTPForgotPassword) {
		if (dem2 == 1) {
			map.addAttribute("ERROR", "	Mã hết hạn, yêu cầu thao tác lại");
			/////////////////////////////////
			/// mới viết thêm
			this.dem2 = 0;
			stopCountdown2 = true;
			return "redirect:/user/ForgotPassword";
		}
		if (randomNumber2 == OTPForgotPassword) {
			System.out.println("tt");
			System.out.println(to2);
			System.out.println(passwordNew);
			stopCountdown2 = true;
			this.dem2 = 0;
			Optional<User> userFind = userService.findByEmail(to2);
			if (userFind.isPresent()) {
				User updateUser = userFind.get();
				updateUser.setPassword(passwordNew);
				userService.save(updateUser);
				return "redirect:/user/login";
			} else {
				System.out.println("hỏng");
				return "redirect:/user/login";
			}

		} else {
			this.dem2 = 0;
			stopCountdown2 = true;
			System.out.println("fail");
			return "redirect:/user/login";
		}

	}
}
