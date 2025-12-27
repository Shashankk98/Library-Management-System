package com.library.loan.service;

import com.library.loan.client.BookClient;
import com.library.loan.client.UserClient;
import com.library.loan.dto.BookDTO;
import com.library.loan.dto.UserDTO;
import com.library.loan.messaging.LoanEventPublisher;
import com.library.loan.model.Loan;
import com.library.loan.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookClient bookClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private LoanEventPublisher loanEventPublisher;

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public List<Loan> getLoansByBookId(Long bookId) {
        return loanRepository.findByBookId(bookId);
    }

    public Loan borrowBook(Long bookId, Long userId) {
        // Verify user exists
        UserDTO user = userClient.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        // Verify book exists and is available
        BookDTO book = bookClient.getBookById(bookId);
        if (book == null) {
            throw new RuntimeException("Book not found with id: " + bookId);
        }

        // Decrease book availability
        Boolean decreased = bookClient.decreaseAvailability(bookId);
        if (!decreased) {
            throw new RuntimeException("Book is not available for borrowing");
        }

        // Create loan record
        Loan loan = new Loan();
        loan.setBookId(bookId);
        loan.setUserId(userId);
        loan.setBorrowDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14)); // 2 weeks loan period
        loan.setStatus(Loan.LoanStatus.ACTIVE);

        Loan savedLoan = loanRepository.save(loan);
        loanEventPublisher.publishLoanCreated(savedLoan);

        return savedLoan;
    }

    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + loanId));

        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new RuntimeException("Loan is not active");
        }

        // Increase book availability
        bookClient.increaseAvailability(loan.getBookId());

        // Update loan record
        loan.setReturnDate(LocalDate.now());
        loan.setStatus(Loan.LoanStatus.RETURNED);

        Loan updatedLoan = loanRepository.save(loan);
        loanEventPublisher.publishLoanReturned(updatedLoan);

        return updatedLoan;
    }
}
