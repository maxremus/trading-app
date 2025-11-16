package com.tradingapp.tradingapp.jobs;

import com.tradingapp.tradingapp.entities.Product;
import com.tradingapp.tradingapp.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CacheConfig;


@Component
@CacheConfig(cacheNames = "products")
public class StockCheckJob {

    private static final Logger log = LoggerFactory.getLogger(StockCheckJob.class);

    private final ProductRepository productRepository;

    @CacheEvict(allEntries = true)
    @Scheduled(fixedRate = 21600000) // 6 часа = 6 * 60 * 60 * 1000
    public void clearProductCache() {

        log.info("The product cache has been cleared automatically.");
    }

    public StockCheckJob(ProductRepository productRepository) {

        this.productRepository = productRepository;
    }

    //Изпълнява се всеки ден в 8:00 сутринта
    @Scheduled(cron = "0 0 8 * * *")
    public void checkLowStock() {

        log.info("Low product availability check is starting...");

        List<Product> lowStockProducts = productRepository.findAll()
                .stream()
                .filter(p -> p.getQuantity() <= 5)
                .toList();

        if (lowStockProducts.isEmpty()) {
            log.info("All products are in normal availability.");
        } else {
            log.warn("Low stock products found:");
            lowStockProducts.forEach(p ->
                    log.warn(" - {} ({}) – remaining {} No.", p.getName(), p.getCategory(), p.getQuantity()));
        }

        log.info("Low availability check completed.");
    }
}
