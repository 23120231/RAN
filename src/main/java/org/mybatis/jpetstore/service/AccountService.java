/*
 *    Copyright 2010-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.jpetstore.service;

import java.util.Optional;

import org.mybatis.jpetstore.domain.Account;
import org.mybatis.jpetstore.mapper.AccountMapper;
import org.mybatis.jpetstore.mapper.CategoryMapper;
import org.mybatis.jpetstore.mapper.ItemMapper;
import org.mybatis.jpetstore.mapper.LineItemMapper;
import org.mybatis.jpetstore.mapper.OrderMapper;
import org.mybatis.jpetstore.mapper.ProductMapper;
import org.mybatis.jpetstore.mapper.SequenceMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class AccountService.
 *
 * @author Eduardo Macarron
 */

 @Service
 public class AccountService {
     private final AccountMapper accountMapper;
     private final CategoryMapper categoryMapper;
     private final ItemMapper itemMapper;
     private final ProductMapper productMapper;
     private final OrderMapper orderMapper;
     private final SequenceMapper sequenceMapper;
     private final LineItemMapper lineItemMapper;

     public AccountService(AccountMapper accountMapper, CategoryMapper categoryMapper, ItemMapper itemMapper, ProductMapper productMapper, OrderMapper orderMapper, SequenceMapper sequenceMapper, LineItemMapper lineItemMapper) {
         this.accountMapper = accountMapper;
         this.categoryMapper = categoryMapper;
         this.itemMapper = itemMapper;
         this.productMapper = productMapper;
         this.orderMapper = orderMapper;
         this.sequenceMapper = sequenceMapper;
         this.lineItemMapper = lineItemMapper;
     }

     // Account methods
     public Account getAccount(String username) {
         return accountMapper.getAccountByUsername(username);
     }

     public Account getAccount(String username, String password) {
         return accountMapper.getAccountByUsernameAndPassword(username, password);
     }

     @Transactional
     public void insertAccount(Account account) {
         accountMapper.insertAccount(account);
         accountMapper.insertProfile(account);
         accountMapper.insertSignon(account);
     }

     @Transactional
     public void updateAccount(Account account) {
         accountMapper.updateAccount(account);
         accountMapper.updateProfile(account);
         Optional.ofNullable(account.getPassword()).filter(password -> password.length() > 0).ifPresent(password -> accountMapper.updateSignon(account));
     }

     // Catalog methods
     public List<Category> getCategoryList() {
         return categoryMapper.getCategoryList();
     }

     public Category getCategory(String categoryId) {
         return categoryMapper.getCategory(categoryId);
     }

     public Product getProduct(String productId) {
         return productMapper.getProduct(productId);
     }

     public List<Product> getProductListByCategory(String categoryId) {
         return productMapper.getProductListByCategory(categoryId);
     }

     public List<Product> searchProductList(String keywords) {
         List<Product> products = new ArrayList<>();
         for (String keyword : keywords.split("\\s+")) {
             products.addAll(productMapper.searchProductList("%" + keyword.toLowerCase() + "%"));
         }
         return products;
     }

     public List<Item> getItemListByProduct(String productId) {
         return itemMapper.getItemListByProduct(productId);
     }

     public Item getItem(String itemId) {
         return itemMapper.getItem(itemId);
     }

     public boolean isItemInStock(String itemId) {
         return itemMapper.getInventoryQuantity(itemId) > 0;
     }

     // Order methods
     @Transactional
     public void insertOrder(Order order) {
         order.setOrderId(getNextId("ordernum"));
         order.getLineItems().forEach(lineItem -> {
             String itemId = lineItem.getItemId();
             Integer increment = lineItem.getQuantity();
             Map<String, Object> param = new HashMap<>(2);
             param.put("itemId", itemId);
             param.put("increment", increment);
             itemMapper.updateInventoryQuantity(param);
         });
         orderMapper.insertOrder(order);
         orderMapper.insertOrderStatus(order);
         order.getLineItems().forEach(lineItem -> {
             lineItem.setOrderId(order.getOrderId());
             lineItemMapper.insertLineItem(lineItem);
         });
     }

     @Transactional
     public Order getOrder(int orderId) {
         Order order = orderMapper.getOrder(orderId);
         order.setLineItems(lineItemMapper.getLineItemsByOrderId(orderId));
         order.getLineItems().forEach(lineItem -> {
             Item item = itemMapper.getItem(lineItem.getItemId());
             item.setQuantity(itemMapper.getInventoryQuantity(lineItem.getItemId()));
             lineItem.setItem(item);
         });
         return order;
     }

     public List<Order> getOrdersByUsername(String username) {
         return orderMapper.getOrdersByUsername(username);
     }

     public int getNextId(String name) {
         Sequence sequence = sequenceMapper.getSequence(new Sequence(name, -1));
         if (sequence == null) {
             throw new RuntimeException("Error: A null sequence was returned from the database (could not get next " + name + " sequence).");
         }
         Sequence parameterObject = new Sequence(name, sequence.getNextId() + 1);
         sequenceMapper.updateSequence(parameterObject);
         return sequence.getNextId();
     }
 }











 /*******************BELOW THIS WAS ORIGINAL  */
@Service
// public class AccountService {

//   private final AccountMapper accountMapper;

//   public AccountService(AccountMapper accountMapper) {
//     this.accountMapper = accountMapper;
//   }

//   public Account getAccount(String username) {
//     return accountMapper.getAccountByUsername(username);
//   }

//   public Account getAccount(String username, String password) {
//     return accountMapper.getAccountByUsernameAndPassword(username, password);
//   }

//   /**
//    * Insert account.
//    *
//    * @param account
//    *          the account
//    */
//   @Transactional
//   public void insertAccount(Account account) {
//     accountMapper.insertAccount(account);
//     accountMapper.insertProfile(account);
//     accountMapper.insertSignon(account);
//   }

//   /**
//    * Update account.
//    *
//    * @param account
//    *          the account
//    */
//   @Transactional
//   public void updateAccount(Account account) {
//     accountMapper.updateAccount(account);
//     accountMapper.updateProfile(account);

//     Optional.ofNullable(account.getPassword()).filter(password -> password.length() > 0)
//         .ifPresent(password -> accountMapper.updateSignon(account));
//   }

// }
