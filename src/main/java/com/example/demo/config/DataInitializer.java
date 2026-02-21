package com.example.demo.config;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ShopRepository shopRepository;
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final ReviewRepository reviewRepository;
    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(String... args) {
        // Check if data already exists
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping initialization.");
            return;
        }
        
        log.info("Initializing database with sample data...");
        
        // 1. Create Users
        List<User> users = createUsers();
        log.info("Created {} users", users.size());
        
        // 2. Create Categories
        List<Category> categories = createCategories();
        log.info("Created {} categories", categories.size());
        
        // 3. Create Shops
        List<Shop> shops = createShops(users, categories);
        log.info("Created {} shops", shops.size());
        
        // 4. Create Menus
        List<Menu> menus = createMenus(shops);
        log.info("Created {} menus", menus.size());
        
        // 5. Create Menu Items
        List<MenuItem> menuItems = createMenuItems(menus);
        log.info("Created {} menu items", menuItems.size());
        
        // 6. Create Reviews
        List<Review> reviews = createReviews(users, shops);
        log.info("Created {} reviews", reviews.size());
        
        // 7. Create Blogs
        List<Blog> blogs = createBlogs(users);
        log.info("Created {} blogs", blogs.size());
        
        // 8. Create Comments
        List<Comment> comments = createComments(users, blogs);
        log.info("Created {} comments", comments.size());
        
        log.info("Database initialization completed successfully!");
    }
    
    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        
        // Admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("System Administrator");
        admin.setEmail("admin@fnb.com");
        admin.setPhone("0901234567");
        admin.setAvatarUrl("https://i.pravatar.cc/150?img=1");
        admin.setRole("admin");
        users.add(userRepository.save(admin));
        
        // Shop owners
        User owner1 = new User();
        owner1.setUsername("owner_cafe");
        owner1.setPassword(passwordEncoder.encode("password123"));
        owner1.setFullName("Nguyễn Văn An");
        owner1.setEmail("owner1@fnb.com");
        owner1.setPhone("0902345678");
        owner1.setAvatarUrl("https://i.pravatar.cc/150?img=11");
        owner1.setRole("owner");
        users.add(userRepository.save(owner1));
        
        User owner2 = new User();
        owner2.setUsername("owner_restaurant");
        owner2.setPassword(passwordEncoder.encode("password123"));
        owner2.setFullName("Trần Thị Bình");
        owner2.setEmail("owner2@fnb.com");
        owner2.setPhone("0903456789");
        owner2.setAvatarUrl("https://i.pravatar.cc/150?img=12");
        owner2.setRole("owner");
        users.add(userRepository.save(owner2));
        
        User owner3 = new User();
        owner3.setUsername("owner_fastfood");
        owner3.setPassword(passwordEncoder.encode("password123"));
        owner3.setFullName("Lê Văn Cường");
        owner3.setEmail("owner3@fnb.com");
        owner3.setPhone("0904567890");
        owner3.setAvatarUrl("https://i.pravatar.cc/150?img=13");
        owner3.setRole("owner");
        users.add(userRepository.save(owner3));
        
        // Regular users
        String[] fullNames = {
            "Phạm Thị Diễm", "Hoàng Văn Em", "Võ Thị Phương",
            "Đặng Văn Giang", "Bùi Thị Hạnh", "Dương Văn Khoa",
            "Phan Thị Lan", "Ngô Văn Minh", "Lý Thị Nga"
        };
        
        for (int i = 0; i < fullNames.length; i++) {
            User user = new User();
            user.setUsername("user" + (i + 1));
            user.setPassword(passwordEncoder.encode("password123"));
            user.setFullName(fullNames[i]);
            user.setEmail("user" + (i + 1) + "@gmail.com");
            user.setPhone("090" + (5000000 + i));
            user.setAvatarUrl("https://i.pravatar.cc/150?img=" + (20 + i));
            user.setRole("user");
            users.add(userRepository.save(user));
        }
        
        return users;
    }
    
    private List<Category> createCategories() {
        List<Category> categories = new ArrayList<>();
        
        String[][] categoryData = {
            {"Cà phê", "Các quán cà phê, trà sữa, đồ uống"},
            {"Quán ăn Việt", "Ẩm thực Việt Nam truyền thống"},
            {"Fast Food", "Đồ ăn nhanh, burger, pizza"},
            {"Lẩu & Nướng", "Quán lẩu và đồ nướng"},
            {"Bánh & Tráng miệng", "Tiệm bánh, kem, tráng miệng"},
            {"Ẩm thực Á", "Món ăn Nhật, Hàn, Trung, Thái"},
            {"Nhà hàng sang trọng", "Fine dining, món Âu"}
        };
        
        for (String[] data : categoryData) {
            Category category = new Category();
            category.setName(data[0]);
            category.setDescription(data[1]);
            categories.add(categoryRepository.save(category));
        }
        
        return categories;
    }
    
    private List<Shop> createShops(List<User> users, List<Category> categories) {
        List<Shop> shops = new ArrayList<>();
        
        // Coffee shops
        Shop cafe1 = new Shop();
        cafe1.setOwner(users.get(1)); // owner1
        cafe1.setCategory(categories.get(0)); // Cà phê
        cafe1.setName("Highlands Coffee Nguyễn Huệ");
        cafe1.setAddress("123 Nguyễn Huệ, Quận 1, TP.HCM");
        cafe1.setLatitude(new BigDecimal("10.7769"));
        cafe1.setLongitude(new BigDecimal("106.7009"));
        cafe1.setOpenTime(LocalTime.of(7, 0));
        cafe1.setCloseTime(LocalTime.of(23, 0));
        cafe1.setStatus("active");
        cafe1.setImages(Arrays.asList(
            "https://picsum.photos/800/600?random=1",
            "https://picsum.photos/800/600?random=2"
        ));
        shops.add(shopRepository.save(cafe1));
        
        Shop cafe2 = new Shop();
        cafe2.setOwner(users.get(1));
        cafe2.setCategory(categories.get(0));
        cafe2.setName("The Coffee House Lý Tự Trọng");
        cafe2.setAddress("45 Lý Tự Trọng, Quận 1, TP.HCM");
        cafe2.setLatitude(new BigDecimal("10.7733"));
        cafe2.setLongitude(new BigDecimal("106.6995"));
        cafe2.setOpenTime(LocalTime.of(7, 30));
        cafe2.setCloseTime(LocalTime.of(22, 30));
        cafe2.setStatus("active");
        cafe2.setImages(Arrays.asList(
            "https://picsum.photos/800/600?random=3",
            "https://picsum.photos/800/600?random=4"
        ));
        shops.add(shopRepository.save(cafe2));
        
        // Vietnamese restaurants
        Shop restaurant1 = new Shop();
        restaurant1.setOwner(users.get(2)); // owner2
        restaurant1.setCategory(categories.get(1)); // Quán ăn Việt
        restaurant1.setName("Phở 24 Pasteur");
        restaurant1.setAddress("5 Pasteur, Quận 1, TP.HCM");
        restaurant1.setLatitude(new BigDecimal("10.7797"));
        restaurant1.setLongitude(new BigDecimal("106.6993"));
        restaurant1.setOpenTime(LocalTime.of(6, 0));
        restaurant1.setCloseTime(LocalTime.of(22, 0));
        restaurant1.setStatus("active");
        restaurant1.setImages(Arrays.asList(
            "https://picsum.photos/800/600?random=5",
            "https://picsum.photos/800/600?random=6",
            "https://picsum.photos/800/600?random=7"
        ));
        shops.add(shopRepository.save(restaurant1));
        
        Shop restaurant2 = new Shop();
        restaurant2.setOwner(users.get(2));
        restaurant2.setCategory(categories.get(1));
        restaurant2.setName("Cơm Tấm Sườn Nướng Ba Ghiền");
        restaurant2.setAddress("89 Nguyễn Trãi, Quận 1, TP.HCM");
        restaurant2.setLatitude(new BigDecimal("10.7632"));
        restaurant2.setLongitude(new BigDecimal("106.6879"));
        restaurant2.setOpenTime(LocalTime.of(10, 0));
        restaurant2.setCloseTime(LocalTime.of(21, 30));
        restaurant2.setStatus("active");
        restaurant2.setImages(Arrays.asList(
            "https://picsum.photos/800/600?random=8",
            "https://picsum.photos/800/600?random=9"
        ));
        shops.add(shopRepository.save(restaurant2));
        
        // Fast food
        Shop fastfood1 = new Shop();
        fastfood1.setOwner(users.get(3)); // owner3
        fastfood1.setCategory(categories.get(2)); // Fast Food
        fastfood1.setName("Burger King Vincom");
        fastfood1.setAddress("72 Lê Thánh Tôn, Quận 1, TP.HCM");
        fastfood1.setLatitude(new BigDecimal("10.7796"));
        fastfood1.setLongitude(new BigDecimal("106.7020"));
        fastfood1.setOpenTime(LocalTime.of(9, 0));
        fastfood1.setCloseTime(LocalTime.of(22, 0));
        fastfood1.setStatus("active");
        fastfood1.setImages(Arrays.asList(
            "https://picsum.photos/800/600?random=10",
            "https://picsum.photos/800/600?random=11"
        ));
        shops.add(shopRepository.save(fastfood1));
        
        // Hot pot
        Shop hotpot = new Shop();
        hotpot.setOwner(users.get(2));
        hotpot.setCategory(categories.get(3)); // Lẩu & Nướng
        hotpot.setName("Lẩu Phan Đình Phùng");
        hotpot.setAddress("234 Phan Đình Phùng, Quận Phú Nhuận, TP.HCM");
        hotpot.setLatitude(new BigDecimal("10.7979"));
        hotpot.setLongitude(new BigDecimal("106.6791"));
        hotpot.setOpenTime(LocalTime.of(17, 0));
        hotpot.setCloseTime(LocalTime.of(23, 0));
        hotpot.setStatus("active");
        hotpot.setImages(Arrays.asList(
            "https://picsum.photos/800/600?random=12",
            "https://picsum.photos/800/600?random=13"
        ));
        shops.add(shopRepository.save(hotpot));
        
        // Bakery
        Shop bakery = new Shop();
        bakery.setOwner(users.get(1));
        bakery.setCategory(categories.get(4)); // Bánh & Tráng miệng
        bakery.setName("ABC Bakery Hai Bà Trưng");
        bakery.setAddress("67 Hai Bà Trưng, Quận 1, TP.HCM");
        bakery.setLatitude(new BigDecimal("10.7744"));
        bakery.setLongitude(new BigDecimal("106.7011"));
        bakery.setOpenTime(LocalTime.of(6, 30));
        bakery.setCloseTime(LocalTime.of(21, 0));
        bakery.setStatus("active");
        bakery.setImages(Arrays.asList(
            "https://picsum.photos/800/600?random=14"
        ));
        shops.add(shopRepository.save(bakery));
        
        return shops;
    }
    
    private List<Menu> createMenus(List<Shop> shops) {
        List<Menu> menus = new ArrayList<>();
        
        for (Shop shop : shops) {
            Menu menu = new Menu();
            menu.setShop(shop);
            menu.setName("Menu chính - " + shop.getName());
            menu.setImages(Arrays.asList(
                "https://picsum.photos/1200/800?random=" + (100 + shop.getId())
            ));
            menus.add(menuRepository.save(menu));
        }
        
        return menus;
    }
    
    private List<MenuItem> createMenuItems(List<Menu> menus) {
        List<MenuItem> items = new ArrayList<>();
        
        // Coffee menu items
        if (menus.size() > 0) {
            items.addAll(createCoffeeItems(menus.get(0)));
            items.addAll(createCoffeeItems(menus.get(1)));
        }
        
        // Pho menu items
        if (menus.size() > 2) {
            items.addAll(createPhoItems(menus.get(2)));
        }
        
        // Com tam items
        if (menus.size() > 3) {
            items.addAll(createComTamItems(menus.get(3)));
        }
        
        // Fast food items
        if (menus.size() > 4) {
            items.addAll(createFastFoodItems(menus.get(4)));
        }
        
        // Hot pot items
        if (menus.size() > 5) {
            items.addAll(createHotPotItems(menus.get(5)));
        }
        
        // Bakery items
        if (menus.size() > 6) {
            items.addAll(createBakeryItems(menus.get(6)));
        }
        
        return items;
    }
    
    private List<MenuItem> createCoffeeItems(Menu menu) {
        List<MenuItem> items = new ArrayList<>();
        
        String[][] coffeeData = {
            {"Cà phê đen", "Cà phê phin truyền thống đậm đà", "25000", "true", "true", "false"},
            {"Cà phê sữa", "Cà phê phin kết hợp sữa đặc", "28000", "true", "true", "false"},
            {"Bạc xỉu", "Sữa tươi pha cà phê nhẹ nhàng", "30000", "true", "false", "false"},
            {"Cappuccino", "Cà phê espresso với sữa foam", "45000", "true", "false", "false"},
            {"Trà sữa trân châu", "Trà sữa trân châu đường đen", "35000", "true", "true", "false"},
            {"Smoothie xoài", "Sinh tố xoài tươi mát lạnh", "40000", "true", "false", "true"}
        };
        
        for (int i = 0; i < coffeeData.length; i++) {
            String[] data = coffeeData[i];
            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setName(data[0]);
            item.setDescription(data[1]);
            item.setPrice(new BigDecimal(data[2]));
            item.setImages(Arrays.asList(
                "https://picsum.photos/600/400?random=" + (200 + i)
            ));
            item.setIsAvailable(Boolean.parseBoolean(data[3]));
            item.setIsHot(Boolean.parseBoolean(data[4]));
            item.setIsSignature(Boolean.parseBoolean(data[5]));
            item.setViewCount(50 + (i * 10));
            items.add(menuItemRepository.save(item));
        }
        
        return items;
    }
    
    private List<MenuItem> createPhoItems(Menu menu) {
        List<MenuItem> items = new ArrayList<>();
        
        String[][] phoData = {
            {"Phở bò tái", "Phở bò với thịt tái mềm", "65000", "true", "true", "true"},
            {"Phở bò chín", "Phở bò với thịt chín", "65000", "true", "false", "false"},
            {"Phở gà", "Phở gà thơm ngon", "60000", "true", "false", "false"},
            {"Phở đặc biệt", "Phở bò đầy đủ các loại", "75000", "true", "true", "true"},
            {"Nem rán", "Chả giò giòn rụm", "45000", "true", "false", "false"},
            {"Gỏi cuốn", "Gỏi cuốn tôm thịt", "40000", "true", "false", "false"}
        };
        
        for (int i = 0; i < phoData.length; i++) {
            String[] data = phoData[i];
            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setName(data[0]);
            item.setDescription(data[1]);
            item.setPrice(new BigDecimal(data[2]));
            item.setImages(Arrays.asList(
                "https://picsum.photos/600/400?random=" + (300 + i)
            ));
            item.setIsAvailable(Boolean.parseBoolean(data[3]));
            item.setIsHot(Boolean.parseBoolean(data[4]));
            item.setIsSignature(Boolean.parseBoolean(data[5]));
            item.setViewCount(80 + (i * 15));
            items.add(menuItemRepository.save(item));
        }
        
        return items;
    }
    
    private List<MenuItem> createComTamItems(Menu menu) {
        List<MenuItem> items = new ArrayList<>();
        
        String[][] comTamData = {
            {"Cơm tấm sườn nướng", "Cơm tấm với sườn nướng thơm ngon", "45000", "true", "true", "true"},
            {"Cơm tấm bì", "Cơm tấm với bì", "40000", "true", "false", "false"},
            {"Cơm tấm đặc biệt", "Cơm tấm đầy đủ topping", "55000", "true", "true", "false"},
            {"Cơm tấm chả", "Cơm tấm với chả trứng", "42000", "true", "false", "false"}
        };
        
        for (int i = 0; i < comTamData.length; i++) {
            String[] data = comTamData[i];
            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setName(data[0]);
            item.setDescription(data[1]);
            item.setPrice(new BigDecimal(data[2]));
            item.setImages(Arrays.asList(
                "https://picsum.photos/600/400?random=" + (400 + i)
            ));
            item.setIsAvailable(Boolean.parseBoolean(data[3]));
            item.setIsHot(Boolean.parseBoolean(data[4]));
            item.setIsSignature(Boolean.parseBoolean(data[5]));
            item.setViewCount(70 + (i * 12));
            items.add(menuItemRepository.save(item));
        }
        
        return items;
    }
    
    private List<MenuItem> createFastFoodItems(Menu menu) {
        List<MenuItem> items = new ArrayList<>();
        
        String[][] fastFoodData = {
            {"Whopper Burger", "Burger bò flame-grilled đặc trưng", "89000", "true", "true", "true"},
            {"Cheese Burger", "Burger phô mai thơm ngon", "65000", "true", "false", "false"},
            {"Chicken Burger", "Burger gà giòn", "69000", "true", "false", "false"},
            {"French Fries", "Khoai tây chiên giòn", "35000", "true", "true", "false"},
            {"Onion Rings", "Hành tây chiên giòn", "38000", "true", "false", "false"},
            {"Pepsi", "Nước ngọt Pepsi", "20000", "true", "false", "false"}
        };
        
        for (int i = 0; i < fastFoodData.length; i++) {
            String[] data = fastFoodData[i];
            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setName(data[0]);
            item.setDescription(data[1]);
            item.setPrice(new BigDecimal(data[2]));
            item.setImages(Arrays.asList(
                "https://picsum.photos/600/400?random=" + (500 + i)
            ));
            item.setIsAvailable(Boolean.parseBoolean(data[3]));
            item.setIsHot(Boolean.parseBoolean(data[4]));
            item.setIsSignature(Boolean.parseBoolean(data[5]));
            item.setViewCount(100 + (i * 20));
            items.add(menuItemRepository.save(item));
        }
        
        return items;
    }
    
    private List<MenuItem> createHotPotItems(Menu menu) {
        List<MenuItem> items = new ArrayList<>();
        
        String[][] hotPotData = {
            {"Lẩu Thái", "Lẩu Thái chua cay đặc trưng", "299000", "true", "true", "true"},
            {"Lẩu bò", "Lẩu bò ngon", "249000", "true", "true", "false"},
            {"Lẩu hải sản", "Lẩu hải sản tươi sống", "349000", "true", "false", "true"},
            {"Combo rau", "Combo rau đầy đủ cho lẩu", "69000", "true", "false", "false"}
        };
        
        for (int i = 0; i < hotPotData.length; i++) {
            String[] data = hotPotData[i];
            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setName(data[0]);
            item.setDescription(data[1]);
            item.setPrice(new BigDecimal(data[2]));
            item.setImages(Arrays.asList(
                "https://picsum.photos/600/400?random=" + (600 + i)
            ));
            item.setIsAvailable(Boolean.parseBoolean(data[3]));
            item.setIsHot(Boolean.parseBoolean(data[4]));
            item.setIsSignature(Boolean.parseBoolean(data[5]));
            item.setViewCount(60 + (i * 10));
            items.add(menuItemRepository.save(item));
        }
        
        return items;
    }
    
    private List<MenuItem> createBakeryItems(Menu menu) {
        List<MenuItem> items = new ArrayList<>();
        
        String[][] bakeryData = {
            {"Bánh mì sandwich", "Bánh mì sandwich thơm ngon", "35000", "true", "true", "false"},
            {"Bánh croissant", "Bánh croissant bơ", "28000", "true", "false", "false"},
            {"Bánh mousse", "Bánh mousse socola", "45000", "true", "false", "true"},
            {"Bánh tiramisu", "Bánh tiramisu Italia", "55000", "true", "true", "true"}
        };
        
        for (int i = 0; i < bakeryData.length; i++) {
            String[] data = bakeryData[i];
            MenuItem item = new MenuItem();
            item.setMenu(menu);
            item.setName(data[0]);
            item.setDescription(data[1]);
            item.setPrice(new BigDecimal(data[2]));
            item.setImages(Arrays.asList(
                "https://picsum.photos/600/400?random=" + (700 + i)
            ));
            item.setIsAvailable(Boolean.parseBoolean(data[3]));
            item.setIsHot(Boolean.parseBoolean(data[4]));
            item.setIsSignature(Boolean.parseBoolean(data[5]));
            item.setViewCount(45 + (i * 8));
            items.add(menuItemRepository.save(item));
        }
        
        return items;
    }
    
    private List<Review> createReviews(List<User> users, List<Shop> shops) {
        List<Review> reviews = new ArrayList<>();
        
        // User 4 reviews shop 1
        Review review1 = new Review();
        review1.setUser(users.get(4));
        review1.setShop(shops.get(0));
        review1.setRating((short) 5);
        review1.setContent("Quán cà phê rất tuyệt vời! Không gian đẹp, nhân viên thân thiện, đồ uống ngon.");
        review1.setReplies(Arrays.asList(
            createReply(users.get(1).getId(), users.get(1).getFullName(), "Cảm ơn bạn đã ghé thăm quán!")
        ));
        reviews.add(reviewRepository.save(review1));
        
        // User 5 reviews shop 1
        Review review2 = new Review();
        review2.setUser(users.get(5));
        review2.setShop(shops.get(0));
        review2.setRating((short) 4);
        review2.setContent("Cà phê ngon, giá hợp lý. Sẽ quay lại.");
        reviews.add(reviewRepository.save(review2));
        
        // User 6 reviews shop 2
        Review review3 = new Review();
        review3.setUser(users.get(6));
        review3.setShop(shops.get(1));
        review3.setRating((short) 5);
        review3.setContent("The Coffee House luôn là sự lựa chọn yêu thích của mình!");
        reviews.add(reviewRepository.save(review3));
        
        // User 7 reviews shop 3 (Pho)
        Review review4 = new Review();
        review4.setUser(users.get(7));
        review4.setShop(shops.get(2));
        review4.setRating((short) 5);
        review4.setContent("Phở ngon, nước dùng đậm đà, thịt bò tươi ngon. Highly recommended!");
        review4.setReplies(Arrays.asList(
            createReply(users.get(2).getId(), users.get(2).getFullName(), "Cảm ơn quý khách! Hẹn gặp lại!")
        ));
        reviews.add(reviewRepository.save(review4));
        
        // User 8 reviews shop 3
        Review review5 = new Review();
        review5.setUser(users.get(8));
        review5.setShop(shops.get(2));
        review5.setRating((short) 4);
        review5.setContent("Phở khá ngon, nhưng hơi đông người vào giờ cao điểm.");
        reviews.add(reviewRepository.save(review5));
        
        // User 9 reviews shop 4 (Com tam)
        Review review6 = new Review();
        review6.setUser(users.get(9));
        review6.setShop(shops.get(3));
        review6.setRating((short) 5);
        review6.setContent("Cơm tấm sườn nướng ngon tuyệt vời! Sườn mềm, nước mắm pha vừa miệng.");
        reviews.add(reviewRepository.save(review6));
        
        // User 10 reviews shop 5 (Fast food)
        Review review7 = new Review();
        review7.setUser(users.get(10));
        review7.setShop(shops.get(4));
        review7.setRating((short) 4);
        review7.setContent("Burger ngon, phục vụ nhanh. Giá hơi cao một chút.");
        reviews.add(reviewRepository.save(review7));
        
        // User 11 reviews shop 6 (Hot pot)
        Review review8 = new Review();
        review8.setUser(users.get(11));
        review8.setShop(shops.get(5));
        review8.setRating((short) 5);
        review8.setContent("Lẩu Thái cực kỳ ngon! Nước lẩu chuẩn vị, hải sản tươi sống.");
        reviews.add(reviewRepository.save(review8));
        
        return reviews;
    }
    
    private Reply createReply(Long userId, String userName, String content) {
        Reply reply = new Reply();
        reply.setUserId(userId);
        reply.setUserName(userName);
        reply.setContent(content);
        reply.setCreatedAt(LocalDateTime.now());
        return reply;
    }
    
    private List<Blog> createBlogs(List<User> users) {
        List<Blog> blogs = new ArrayList<>();
        
        Blog blog1 = new Blog();
        blog1.setAuthor(users.get(4));
        blog1.setTitle("Top 10 quán cà phê đẹp nhất Sài Gòn");
        blog1.setContent("Sài Gòn không chỉ nổi tiếng với nhịp sống sôi động mà còn có rất nhiều quán cà phê đẹp, view tuyệt vời...\n\n1. Highlands Coffee Nguyễn Huệ - View sông tuyệt đẹp\n2. The Coffee House Lý Tự Trọng - Không gian yên tĩnh\n...");
        blog1.setImages(Arrays.asList(
            "https://picsum.photos/1200/800?random=801",
            "https://picsum.photos/1200/800?random=802"
        ));
        blog1.setLikesCount(156);
        blog1.setStatus("published");
        blogs.add(blogRepository.save(blog1));
        
        Blog blog2 = new Blog();
        blog2.setAuthor(users.get(5));
        blog2.setTitle("Bí quyết chọn phở ngon tại Sài Gòn");
        blog2.setContent("Phở là món ăn sáng phổ biến của người Việt. Để chọn được tô phở ngon, bạn cần chú ý:\n\n- Nước dùng phải trong, ngọt tự nhiên\n- Bánh phở mềm nhưng không nhão\n- Thịt bò tươi ngon...");
        blog2.setImages(Arrays.asList(
            "https://picsum.photos/1200/800?random=803"
        ));
        blog2.setLikesCount(234);
        blog2.setStatus("published");
        blogs.add(blogRepository.save(blog2));
        
        Blog blog3 = new Blog();
        blog3.setAuthor(users.get(6));
        blog3.setTitle("Review chuỗi fast food mới tại Việt Nam");
        blog3.setContent("Năm 2026 đánh dấu sự xuất hiện của nhiều chuỗi fast food mới tại Việt Nam. Hôm nay mình xin review...");
        blog3.setImages(Arrays.asList(
            "https://picsum.photos/1200/800?random=804",
            "https://picsum.photos/1200/800?random=805"
        ));
        blog3.setLikesCount(89);
        blog3.setStatus("published");
        blogs.add(blogRepository.save(blog3));
        
        Blog blog4 = new Blog();
        blog4.setAuthor(users.get(7));
        blog4.setTitle("Ẩm thực đường phố Sài Gòn - Nét văn hóa đặc sắc");
        blog4.setContent("Ẩm thực đường phố là một phần không thể thiếu trong văn hóa Sài Gòn. Từ bánh mì, bún, phở đến các món ăn vặt...");
        blog4.setImages(Arrays.asList(
            "https://picsum.photos/1200/800?random=806"
        ));
        blog4.setLikesCount(312);
        blog4.setStatus("published");
        blogs.add(blogRepository.save(blog4));
        
        return blogs;
    }
    
    private List<Comment> createComments(List<User> users, List<Blog> blogs) {
        List<Comment> comments = new ArrayList<>();
        
        // Comments on blog 1
        Comment comment1 = new Comment();
        comment1.setBlog(blogs.get(0));
        comment1.setUser(users.get(8));
        comment1.setContent("Bài viết rất hữu ích! Mình sẽ thử ghé những quán này.");
        comments.add(commentRepository.save(comment1));
        
        Comment comment2 = new Comment();
        comment2.setBlog(blogs.get(0));
        comment2.setUser(users.get(9));
        comment2.setContent("Highlands Coffee Nguyễn Huệ view thật sự đẹp lắm!");
        comment2.setReplies(Arrays.asList(
            createReply(users.get(4).getId(), users.get(4).getFullName(), "Đúng vậy! Mình cũng rất thích quán đó!")
        ));
        comments.add(commentRepository.save(comment2));
        
        // Comments on blog 2
        Comment comment3 = new Comment();
        comment3.setBlog(blogs.get(1));
        comment3.setUser(users.get(10));
        comment3.setContent("Cảm ơn tác giả đã chia sẻ. Bí quyết rất hay!");
        comments.add(commentRepository.save(comment3));
        
        Comment comment4 = new Comment();
        comment4.setBlog(blogs.get(1));
        comment4.setUser(users.get(11));
        comment4.setContent("Phở 24 là quán mình hay đi nhất. Nước dùng ngọt tự nhiên.");
        comments.add(commentRepository.save(comment4));
        
        // Comments on blog 3
        Comment comment5 = new Comment();
        comment5.setBlog(blogs.get(2));
        comment5.setUser(users.get(4));
        comment5.setContent("Review chi tiết quá! Cảm ơn bạn nhiều.");
        comments.add(commentRepository.save(comment5));
        
        return comments;
    }
}
