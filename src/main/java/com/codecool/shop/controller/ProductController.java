package com.codecool.shop.controller;

import com.codecool.shop.config.TemplateEngineUtil;
import com.codecool.shop.dao.ProductCategoryDao;
import com.codecool.shop.dao.ProductDao;
import com.codecool.shop.dao.SupplierDao;
import com.codecool.shop.service.ActiveDataSourceService;
import com.codecool.shop.service.ProductService;
import com.codecool.shop.service.SupplierService;
import org.slf4j.Logger;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = {"/"})
public class ProductController extends HttpServlet {
    Logger logger = Util.createLogger(ProductController.class);
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ActiveDataSourceService activeDataSourceService = ActiveDataSourceService.getInstance();

        //Daos
        ProductDao productDataStore = activeDataSourceService.getActiveProductDao();
        SupplierDao supplierDataStore = activeDataSourceService.getActiveSupplierDao();
        ProductCategoryDao productCategoryDataStore = activeDataSourceService.getActiveProductCategoryDao();

        //Services
        SupplierService supplierService = new SupplierService(supplierDataStore);
        ProductService productService = new ProductService(productDataStore, productCategoryDataStore);

        TemplateEngine engine = TemplateEngineUtil.getTemplateEngine(req.getServletContext());
        WebContext context = new WebContext(req, resp, req.getServletContext());

        if ((req.getParameter("categoryId") != null) && (req.getParameter("vendorId") == null)) {
            int category_id = Integer.parseInt(req.getParameter("categoryId"));
            context.setVariable("supplier", null);
            context.setVariable("category", productService.getProductCategory(category_id));
            context.setVariable("products", productService.getProductsForCategory(category_id));
            logger.info("Sort by category: {}", productService.getProductCategory(category_id).getName());
        } else if ((req.getParameter("categoryId") == null) && (req.getParameter("supplierId") != null)) {
            int supplierId = Integer.parseInt(req.getParameter("supplierId"));
            context.setVariable("category", null);
            context.setVariable("supplier", supplierService.getSupplier(supplierId));
            context.setVariable("products", productService.getProductsForSupplier(supplierId));
            logger.info("Sort by supplier: {}", supplierService.getSupplier(supplierId).getName());
        } else {
            context.setVariable("supplier", null);
            context.setVariable("category", null);
            context.setVariable("products", productService.getAllProducts());
            logger.info("GET request \"/\".");
        }

        context.setVariable("allcategories", productCategoryDataStore.getAll());
        context.setVariable("allsuppliers", supplierService.getAllSuppliers());

        // Alternative setting of the template context
        // Map<String, Object> params = new HashMap<>();
        // params.put("category", productCategoryDataStore.find(1));
        // params.put("products", productDataStore.getBy(productCategoryDataStore.find(1)));
        // context.setVariables(params);

        engine.process("product/index.html", context, resp.getWriter());
    }

}
