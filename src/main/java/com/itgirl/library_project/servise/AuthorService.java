package com.itgirl.library_project.servise;

import com.itgirl.library_project.Dto.AuthorDto;
import com.itgirl.library_project.Exception.ResourceNotFoundException;
import com.itgirl.library_project.Specification.AuthorSpecification;
import com.itgirl.library_project.entity.Author;
import com.itgirl.library_project.repository.AuthorRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public AuthorDto addNewAuthor(AuthorDto authorDto) {
        Author author = modelMapper.map(authorDto, Author.class);
        Author savedAuthor = authorRepository.save(author);
        return modelMapper.map(savedAuthor, AuthorDto.class);
    }

    @Transactional
    public List<AuthorDto> getAllAuthors(String name, String surname) {
        List<Author> authors = authorRepository.findAll(); // Без фильтра
        return authors.stream()
                .map(author -> modelMapper.map(author, AuthorDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public AuthorDto getAuthorById(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
        return modelMapper.map(author, AuthorDto.class);
    }

    @Transactional
    public AuthorDto updateAuthor(Long id, AuthorDto authorDto) {
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
        modelMapper.map(authorDto, existingAuthor);
        Author updatedAuthor = authorRepository.save(existingAuthor);
        return modelMapper.map(updatedAuthor, AuthorDto.class);
    }

    @Transactional
    public void deleteAuthor(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id " + id));
        authorRepository.delete(author);
    }

    @Transactional
    public List<AuthorDto> getAuthorsByNameOrSurname(String name, String surname) {
        List<Author> authors = authorRepository.findByNameOrSurname(
                StringUtils.isNotEmpty(name) ? name : null,
                StringUtils.isNotEmpty(surname) ? surname : null
        );

        if (authors.isEmpty()) {
            throw new EntityNotFoundException("Authors not found");
        }

        return authors.stream()
                .map(this::toAuthorDto)
                .collect(Collectors.toList());
    }

    private AuthorDto toAuthorDto(Author author) {
        return AuthorDto.builder()
                .id(author.getId())
                .name(author.getName())
                .surname(author.getSurname())
                .build();
    }

    @Transactional
    public List<AuthorDto> getAuthorsByNameWithCriteria(String name, String surname) {
        Specification<Author> specification = Specification.where(null);

        if (!StringUtils.isEmpty(name)) {
            specification = specification.and(AuthorSpecification.hasName(name));
        }
        if (!StringUtils.isEmpty(surname)) {
            specification = specification.and(AuthorSpecification.hasSurname(surname));
        }

        List<Author> authors = authorRepository.findAll(specification);
        log.info("Найдено {} авторов", authors.size());

        return authors.stream()
                .map(author -> {
                    log.info("Маппинг автора: {}", author);
                    return modelMapper.map(author, AuthorDto.class);
                })
                .collect(Collectors.toList());
    }
}


