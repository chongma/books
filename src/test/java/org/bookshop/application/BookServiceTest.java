package org.bookshop.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import javax.inject.Inject;

import org.apache.openwebbeans.junit5.Cdi;
import org.bookshop.entities.Book;
import org.junit.jupiter.api.Test;

@Cdi
class BookServiceTest {

	@Inject
	private BookService bookService;

	@Test
	void test() {
		Book book = new Book();
		book.setBookTitle("test");
		bookService.addBook(book);
		List<Book> books = bookService.getAllBooks();
		assertEquals(1, books.size());
	}

}
