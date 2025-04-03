package com.courseServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.courseServer.enteties.User;

@Repository
public interface UserRepo extends CrudRepository<User, Long>{
}
