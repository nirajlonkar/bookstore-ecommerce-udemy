package com.boostore.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.boostore.domain.BillingAddress;
import com.boostore.domain.CartItem;
import com.boostore.domain.Payment;
import com.boostore.domain.ShippingAddress;
import com.boostore.domain.ShoppingCart;
import com.boostore.domain.User;
import com.boostore.domain.UserBilling;
import com.boostore.domain.UserPayment;
import com.boostore.domain.UserShipping;
import com.boostore.service.BillingAddressService;
import com.boostore.service.CartItemService;
import com.boostore.service.PaymentService;
import com.boostore.service.ShippingAddressService;
import com.boostore.service.UserPaymentService;
import com.boostore.service.UserService;
import com.boostore.service.UserShippingService;
import com.boostore.utility.INDConstants;

@Controller
public class CheckoutController {
	
	public ShippingAddress shippingAddress = new ShippingAddress();
	public BillingAddress billingAddress = new BillingAddress();
	public Payment payment = new Payment();
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private ShippingAddressService shippingAddressService;
	
	@Autowired
	private PaymentService paymentService;

	@Autowired
	private BillingAddressService billingAddressService;
	
	@Autowired
	private UserShippingService userShippingService;
	
	@Autowired
	private UserPaymentService userPaymentService;
	
	
	@RequestMapping("/checkout")
	public String checkout(
			@RequestParam("id")Long cartId,
			@RequestParam(value = "missingRequiredField", required=false) boolean missingRequiredField,
			Model model, Principal principal
			) {
		System.out.print("~~~~~~~~~~~~~~~~~~inside checkout~~~~~~~~~~~~~~~~~~~~~");
		User user = userService.findByUsername(principal.getName());
		
		if(cartId != user.getShoppingCart().getId()) {
			return "badRequestPage";
		}
		
		List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());
		if(cartItemList.size()==0) {
			model.addAttribute("emptyCart", true);
			return "forward:/shoppingCart/cart";
		}
		
		for(CartItem cartItem : cartItemList) {
			if(cartItem.getBook().getInStockNumber() < cartItem.getQty()) {
				model.addAttribute("notEnoughStock",true);
				return "forward:/shoppingCart/cart";
			}
		}
		
		List<UserShipping> userShippingList = user.getUserShippingList();
		List<UserPayment> userPaymentList = user.getUserPaymentList();
		
		model.addAttribute("userShippingList",userShippingList);
		model.addAttribute("userPaymentList",userPaymentList);
		
		if(userPaymentList.size()==0) {
			model.addAttribute("emptyPaymentList",true);
		}else {
			model.addAttribute("emptyPaymentList",false);
		}
		
		if(userShippingList.size()==0) {
			model.addAttribute("emptyShippingList",true);
		}else {
			model.addAttribute("emptyShippingList",false);
		}
		
		ShoppingCart shoppingCart = user.getShoppingCart();
		
		for(UserShipping userShipping : userShippingList) {
			if(userShipping.isUserShippingDefault()) {
				shippingAddressService.setByUserShipping(userShipping, shippingAddress);
			}
		}
		
		for(UserPayment userPayment : userPaymentList) {
			if(userPayment.isDeafaultPayment()) {
				paymentService.setByUserPayment(userPayment, payment);
				billingAddressService.setByUserBilling(billingAddress, userPayment.getUserBilling());
			}
		}
		System.out.println(shippingAddress.toString());
		System.out.println(billingAddress.toString());
		System.out.println(payment.toString());
		
		
		model.addAttribute("shippingAddress", shippingAddress);
		model.addAttribute("payment", payment);
		model.addAttribute("billingAddress", billingAddress);
		model.addAttribute("cartItemList", cartItemList);
		model.addAttribute("shoppingCart", user.getShoppingCart());
		
		List<String> stateList = INDConstants.listOfINDStatesCodes;
		Collections.sort(stateList);
		model.addAttribute("stateList", stateList);
		
		model.addAttribute("classActiveShipping",true);
		
		if(missingRequiredField) {
			model.addAttribute("missingRequiredField",true);
			
		}
		return "checkout";
	}
	
	@RequestMapping("/setShippingAddress")
	public String setShippingAddress(
			@RequestParam("userShippingId")Long userShippingId,
			Principal principal, Model model
			) {
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(userShippingId);
		
		if(userShipping.getUser().getId() != user.getId()) {
			return "badRequestPage";
		}else {
			shippingAddressService.setByUserShipping(userShipping, shippingAddress);

			List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());

			/*
			 * BillingAddress billingAddress = new BillingAddress();
			 */			
			model.addAttribute("shippingAddress", shippingAddress);
			model.addAttribute("payment", payment);
			model.addAttribute("billingAddress", billingAddress);
			model.addAttribute("cartItemList", cartItemList);
			model.addAttribute("shoppingCart", user.getShoppingCart());
			
			List<String> stateList = INDConstants.listOfINDStatesCodes;
			Collections.sort(stateList);
			model.addAttribute("stateList", stateList);
			
			List<UserShipping> userShippingList = user.getUserShippingList();
			List<UserPayment> userPaymentList = user.getUserPaymentList();
			
			model.addAttribute("userShippingList",userShippingList);
			model.addAttribute("userPaymentList",userPaymentList);
			
			model.addAttribute("shippingAddress",shippingAddress);
			
			model.addAttribute("classActiveShipping", true);
			
			if(userPaymentList.size()==0) {
				model.addAttribute("emptyPaymentList",true);
			}else {
				model.addAttribute("emptyPaymentList",false);
			}
			
			
			model.addAttribute("emptyShippingList",false);
			
			
			return "checkout";
			
		}
		
	}
	
	@RequestMapping("/setPaymentMethod")
	public String setPaymentMethod(@RequestParam("userPaymentId") Long userPaymentId, Principal principal, Model model) {
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment =  userPaymentService.findById(userPaymentId);
		UserBilling userBilling = userPayment.getUserBilling();
		
		if(userPayment.getUser().getId() != user.getId()) {
			return "badRequestPage";
		} else {
			paymentService.setByUserPayment(userPayment, payment);
			
			List<CartItem> cartItemList = cartItemService.findByShoppingCart(user.getShoppingCart());

			billingAddressService.setByUserBilling(billingAddress, userBilling);
			
			model.addAttribute("shippingAddress", shippingAddress);
			model.addAttribute("payment", payment);
			model.addAttribute("billingAddress", billingAddress);
			model.addAttribute("cartItemList", cartItemList);
			model.addAttribute("shoppingCart", user.getShoppingCart());
			
			List<String> stateList = INDConstants.listOfINDStatesCodes;
			Collections.sort(stateList);
			model.addAttribute("stateList", stateList);
			
			List<UserShipping> userShippingList = user.getUserShippingList();
			List<UserPayment> userPaymentList = user.getUserPaymentList();
			
			model.addAttribute("userShippingList",userShippingList);
			model.addAttribute("userPaymentList",userPaymentList);
			
			model.addAttribute("shippingAddress",shippingAddress);
			
			model.addAttribute("classActivePayment", true);
			
			model.addAttribute("emptyPaymentList",false);
			
			if(userShippingList.size()==0) {
				model.addAttribute("emptyShippingList",true);
			}else {
				model.addAttribute("emptyShippingList",false);
			}
			
			return "checkout";
		}
		
	}
}
