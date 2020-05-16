package com.boostore.service;

import java.util.List;
import java.util.Optional;

import com.boostore.domain.Book;

public interface BookService {

	List<Book> findAll();

	Book findOne(Integer id);
}
