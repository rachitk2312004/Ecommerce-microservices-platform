package com.enterprise.ecommerce.product.config;

import com.enterprise.ecommerce.product.entity.Category;
import com.enterprise.ecommerce.product.entity.Product;
import com.enterprise.ecommerce.product.repository.CategoryRepository;
import com.enterprise.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Map<String, String> PRODUCT_PRICES_INR = Map.of(
            "Wireless Headphones", "14999.00",
            "Smartphone", "69999.00",
            "Laptop", "99999.00",
            "Classic T-Shirt", "2499.00",
            "Denim Jeans", "5999.00",
            "Winter Jacket", "12999.00",
            "Clean Code", "3999.00",
            "Design Patterns", "4999.00"
    );

    private static final Map<String, String> PRODUCT_IMAGES = Map.of(
            "Wireless Headphones", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=300&fit=crop&q=80",
            "Smartphone", "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=300&fit=crop&q=80",
            "Laptop", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=300&fit=crop&q=80",
            "Classic T-Shirt", "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=300&fit=crop&q=80",
            "Denim Jeans", "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&h=300&fit=crop&q=80",
            "Winter Jacket", "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=300&fit=crop&q=80",
            "Clean Code", "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&h=300&fit=crop&q=80",
            "Design Patterns", "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&h=300&fit=crop&q=80"
    );

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Value("${app.seed.enabled:true}")
    private boolean seedEnabled;

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        repairBrokenImageUrls();
        repairLegacyPrices();

        if (categoryRepository.count() > 0) {
            return;
        }

        Category electronics = categoryRepository.save(Category.builder()
                .name("Electronics")
                .description("Electronic devices and accessories")
                .active(true)
                .build());

        Category clothing = categoryRepository.save(Category.builder()
                .name("Clothing")
                .description("Apparel and fashion items")
                .active(true)
                .build());

        Category books = categoryRepository.save(Category.builder()
                .name("Books")
                .description("Books and publications")
                .active(true)
                .build());

        saveProduct("Wireless Headphones", "Noise-cancelling over-ear wireless headphones",
                "14999.00", electronics.getId());
        saveProduct("Smartphone", "Latest generation smartphone with 128GB storage",
                "69999.00", electronics.getId());
        saveProduct("Laptop", "15-inch laptop with 16GB RAM and 512GB SSD",
                "99999.00", electronics.getId());
        saveProduct("Classic T-Shirt", "Cotton crew-neck t-shirt available in multiple colors",
                "2499.00", clothing.getId());
        saveProduct("Denim Jeans", "Slim-fit denim jeans with stretch fabric",
                "5999.00", clothing.getId());
        saveProduct("Winter Jacket", "Insulated winter jacket with water-resistant shell",
                "12999.00", clothing.getId());
        saveProduct("Clean Code", "A handbook of agile software craftsmanship by Robert C. Martin",
                "3999.00", books.getId());
        saveProduct("Design Patterns", "Elements of reusable object-oriented software",
                "4999.00", books.getId());
    }

    private void saveProduct(String name, String description, String price, Long categoryId) {
        productRepository.save(Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .categoryId(categoryId)
                .imageUrl(PRODUCT_IMAGES.get(name))
                .active(true)
                .build());
    }

    private void repairBrokenImageUrls() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            String imageUrl = product.getImageUrl();
            boolean missing = imageUrl == null || imageUrl.isBlank();
            boolean broken = imageUrl != null
                    && (imageUrl.contains("example.com") || imageUrl.contains("picsum.photos"));
            if (missing || broken) {
                String replacement = PRODUCT_IMAGES.getOrDefault(
                        product.getName(),
                        "https://images.unsplash.com/photo-1472851293608-3e6fd74545c8?w=400&h=300&fit=crop&q=80"
                );
                product.setImageUrl(replacement);
                productRepository.save(product);
            }
        }
    }

    private void repairLegacyPrices() {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            String inrPrice = PRODUCT_PRICES_INR.get(product.getName());
            if (inrPrice != null && product.getPrice().compareTo(new BigDecimal("1000")) < 0) {
                product.setPrice(new BigDecimal(inrPrice));
                productRepository.save(product);
            }
        }
    }
}
