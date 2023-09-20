package com.example.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.ProductDTO;

import com.example.demo.models.Category;

import com.example.demo.models.Product;

import com.example.demo.models.User;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.UserService;

@Controller
@RequestMapping("/product")
public class ProductController {
	@Autowired
	ProductService productService;
	@Autowired
	CategoryService categoryService;
	@Autowired
	UserService userService;

	// load danh mục category
	@ModelAttribute(name = "CATEGORYS")
	public List<Category> getAllCategory() {
		return categoryService.findAll();
	}

	// màn hình crud sp
	@RequestMapping("/productList")
	public String List(ModelMap map, HttpSession session) {
		if (session.getAttribute("USERNAME") != null) {

			String username = (String) session.getAttribute("USERNAME");
			map.addAttribute("NAME", username);
			User user = userService.findIdByUsername(username);
			if (user.getRole().getId() == 2) {
				map.addAttribute("PRODUCT", productService.findAllByUsername(username));
				System.out.println("2");
				return "View_Product";
			} else {
				System.out.println("1");
				System.out.println(user.getRole().getId());
				return "redirect:/sell/ListSell";
			}

		} else {
			return "redirect:/user/login";
		}
	}

	// lọc
	@RequestMapping("/search/{id}")
	public String search(@PathVariable("id") Long id, ModelMap map, HttpSession session) {
		if (id == null) {
			return List(map, session); // Gọi lại phương thức list() để hiển thị danh sách người dùng
		}
		String username = (String) session.getAttribute("USERNAME");
		List<Product> search = productService.findByCategory(username, id);
		map.addAttribute("PRODUCT", search);
		return "View_Product";
	}

	// thêm
	@GetMapping("/")
	public String addOrEdit(ModelMap map) {
		ProductDTO productDTO = new ProductDTO();
		map.addAttribute("PRODUCTDTO", productDTO);
		map.addAttribute("ACTION", "/product/saverProduct");
		return "Product_Create";
	}

	// lưu
	@PostMapping("/saverProduct")
	public String save(ModelMap map, @ModelAttribute("PRODUCTDTO") ProductDTO dto, HttpSession session) {
		// kiểm tra xem Staffs có tồn tại hay chưa
		if (session.getAttribute("USERNAME") != null) {
			String username = (String) session.getAttribute("USERNAME");
			Product optionalStaff = productService.findByMa(dto.getMaSP(),username);
			Product product = null;
			String image = "logo.png";
			Path path = Paths.get("uploads/");

			if (optionalStaff != null) {
				map.addAttribute("ERROR", "	Trùng mã sp ");
				return "Product_Create";

			}

			else {
				// add
				if (!dto.getPhoto().isEmpty()) {
					// nếu ảnh không tồn tại trong uploads thì tải ảnh lên
					Path uploadPath = Paths.get("uploads");
					try {
						InputStream inputStream = dto.getPhoto().getInputStream();
						Files.copy(inputStream, uploadPath.resolve(dto.getPhoto().getOriginalFilename()),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						// update ảnh
						InputStream inputStream = dto.getPhoto().getInputStream();
						Files.copy(inputStream, path.resolve(dto.getPhoto().getOriginalFilename()),
								StandardCopyOption.REPLACE_EXISTING);
						image = dto.getPhoto().getOriginalFilename().toString();
					} catch (Exception e) {
						// Xử lý ngoại lệ
						e.printStackTrace();
					}
				}
			}
			System.out.println(dto.getMaSP());
			System.out.println(dto.getName());
			product = new Product(dto.getMaSP(), dto.getName(), dto.getPrice(), dto.getQuantity(), dto.getDescription(),
					image, dto.getVolume(), new Category(dto.getCategoryId()), userService.findIdByUsername(username));
			productService.save(product);
			System.out.println(userService.findIdByUsername(username));
			return "redirect:/product/productList";
		} else {
			return "redirect:/user/login";
		}
	}

	// delete
	@RequestMapping("/delete/{id}")
	public String delete(ModelMap map, @PathVariable("id") Long id) {
		System.out.println(id);
		productService.deleteById(id);
		return "redirect:/product/productList";

	}

	// edit
	@RequestMapping("/edit/{id}")
	public String edit(ModelMap map, @PathVariable("id") Long id) {
		Optional<Product> opStaff = productService.findById(id);
		ProductDTO dto = null;
		if (opStaff.isPresent()) {
			Product st = opStaff.get();
			File file = new File("uploads/" + st.getPhoto());
			FileInputStream input;
			try {
				// đoạn này dùng để đọc nội dung của một tệp tin từ thư mục "uploads",
				// sau đó chuyển đổi nó thành một đối tượng MultipartFile
				input = new FileInputStream(file);
				MultipartFile multiPhoto = new MockMultipartFile("file", file.getName(), "text/plain",
						org.apache.commons.io.IOUtils.toByteArray(input));
				dto = new ProductDTO(st.getMaSP(), st.getName(), st.getPrice(), st.getQuantity(), st.getDescription(),
						multiPhoto, st.getVolume(), st.getCategory().getId(), st.getUser().getId());
			} catch (Exception e) {
				// TODO: handle exception
			}
			map.addAttribute("PRODUCTDTO", dto);
		} else {
			map.addAttribute("PRODUCTDTO", new ProductDTO());
		}
		map.addAttribute("ACTION", "/product/update/" + id);

		return "Product_Create";

	}

	
	//update
	@RequestMapping(value = "/update/{id}")
	public String update(@PathVariable("id") Long id, @ModelAttribute("PRODUCTDTO") ProductDTO productDTO,
			HttpSession session) {
		Optional<Product> stafffind = productService.findById(id);
		String username = (String) session.getAttribute("USERNAME");
		if (stafffind.isPresent()) {
			String image = "logo.png";
			Path path = Paths.get("uploads/");
			if (productDTO.getPhoto().isEmpty()) {
				image = stafffind.get().getPhoto();
			} else {
				try {
					// nếu ảnh không tồn tại trong uploads thì tải ảnh lên
					Path uploadPath = Paths.get("uploads");
					try {
						InputStream inputStream = productDTO.getPhoto().getInputStream();
						Files.copy(inputStream, uploadPath.resolve(productDTO.getPhoto().getOriginalFilename()),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// update ảnh
					InputStream inputStream = productDTO.getPhoto().getInputStream();
					Files.copy(inputStream, path.resolve(productDTO.getPhoto().getOriginalFilename()),
							StandardCopyOption.REPLACE_EXISTING);
					image = productDTO.getPhoto().getOriginalFilename().toString();
				} catch (Exception e) {
					// Xử lý ngoại lệ
					e.printStackTrace();
				}
			}

			Product updatedProduct = stafffind.get();
			// Cập nhật thông tin người dùng
			updatedProduct.setDescription(productDTO.getDescription());
			updatedProduct.setName(productDTO.getName());
			updatedProduct.setPhoto(image);
			updatedProduct.setPrice(productDTO.getPrice());
			updatedProduct.setVolume(productDTO.getVolume());
			updatedProduct.setQuantity(productDTO.getQuantity());
			updatedProduct.setUser(userService.findIdByUsername(username));
			updatedProduct.setCategory(new Category(productDTO.getCategoryId()));
			productService.save(updatedProduct);
		}
		return "redirect:/product/productList";
	}

	@RequestMapping("/sell")
	public String Sell() {
		return "Sell";
	}

}
