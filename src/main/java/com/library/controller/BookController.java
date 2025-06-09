package com.library.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.library.book.BookDTO;
import com.library.borrow.Borrow;
import com.library.member.MemberVO;
import com.library.review.Review;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.ReviewService;

@Controller
public class BookController {
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private ReviewService reviewService;
	
	@Autowired
	private BorrowService borrowService;
	
	@GetMapping("/list")
    public String listBooks(
            @RequestParam(value = "page",defaultValue = "1") int page,
            @RequestParam(value = "size",defaultValue = "30") int size,
            Model model) {
        if (size >= 200) size = 200; // 최대 200개 제한
        else if (size >= 100) size = 100;
        else if (size >= 50) size = 50;
        else size = 30;
        List<BookDTO> books = bookService.getAllBooks(page, size);
        long totalBooks = bookService.getTotalBooks();
        model.addAttribute("books", books);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "book/list";
    }

    @GetMapping("/list/book/{seqNo}")
    public String getBook(@PathVariable(value = "seqNo") Long seqNo, 
    		@SessionAttribute(value = "loggedInMember", required = false) MemberVO loggedInMember, 
    		Model model) {
        BookDTO book = bookService.getBookById(seqNo);
        List<Review> reviews = bookService.getReviewsByBookSeqNo(seqNo);
        boolean hasBorrowed = false;
        if(loggedInMember != null) {
        	hasBorrowed = borrowService.hasBorrowed(loggedInMember.getMemberId(), seqNo);        	
        }
        model.addAttribute("book", book);
        model.addAttribute("reviews", reviews);
        model.addAttribute("hasBorrowed" ,hasBorrowed);
        return "book/book";
    }

    @GetMapping("/list/search")
    public String searchBooks(
            @RequestParam(value = "query") String query,
            @RequestParam(value = "page",defaultValue = "1") int page,
            @RequestParam(value = "size",defaultValue = "30") int size,
            Model model) {
        if (size >= 200) size = 200;
        else if (size >= 100) size = 100;
        else if (size >= 50) size = 50;
        else size = 30;
        List<BookDTO> books = bookService.searchBooks(query, page, size);
        long totalBooks = bookService.getTotalBooksByTitle(query);
        model.addAttribute("books", books);
        model.addAttribute("totalBooks", totalBooks);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("query", query);
        return "book/list";
    }
    
    @PostMapping("/book/{seqNo}/review")
    public String submitReview(
            @PathVariable(value = "seqNo") Long seqNo,
            @RequestParam(value = "content") String content,
            @SessionAttribute("loggedInMember") MemberVO loggedInMember) {
        reviewService.saveReview(seqNo, loggedInMember.getMemberId(), content);
        return "redirect:/list/book/" + seqNo;
    }
    
    @PostMapping("/book/{seqNo}/borrow")
    public String borrowBook(
            @PathVariable(value = "seqNo") Long seqNo,
            @SessionAttribute("loggedInMember") MemberVO loggedInMember,
            @RequestParam(value = "userId") String userId) {
    	borrowService.saveBorrow(userId, seqNo);
        return "redirect:/list/book/" + seqNo;
    }
    @PostMapping("/book/{seqNo}/return")
    public String returnBook(
    		@PathVariable(value = "seqNo") Long seqNo,
    		@RequestParam(value = "borrowId") Long borrowId
    		) {
    	borrowService.returnBook(seqNo, borrowId);
    	return "member/mypage";
    }
}