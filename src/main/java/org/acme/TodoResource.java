package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Path("/todos")
public class TodoResource {

    private final Logger logger = LoggerFactory.getLogger(TodoResource.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Todo> all() {
        return Todo.listAll();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Todo findById(@PathParam("id") Long id) {
        Optional<PanacheEntityBase> byIdOptional = Todo.findByIdOptional(id);
        if (byIdOptional.isPresent()) {
            return (Todo) byIdOptional.get();
        } else {
            logger.error("Todo with id {} not found", id);
            throw new NotFoundException(
                    Response.status(Response.Status.NOT_FOUND).entity(
                            """
                            {
                                "error": "Todo not found",
                                "message": "Todo with id %d does not exist."
                            }
                            """.formatted(id)
                    ).build()
            );
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Todo create(Todo todo) {
        Todo.persist(todo);
        return todo;
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Todo update(Todo todo) {
        Todo existingTodo = Todo.findById(todo.id);
        if (existingTodo != null) {
            existingTodo.title = todo.title;
            existingTodo.priority = todo.priority;
            existingTodo.done = todo.done;
            Todo.persist(existingTodo);
            return existingTodo;
        }
        throw new NotFoundException("Todo not found with id: " + todo.id);
    }

    @PUT
    @Path(("/complete/{id}"))
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Todo complete(@PathParam("id") Long id) {
        Todo todo = Todo.findById(id);
        if (todo != null) {
            todo.done = true;
            Todo.persist(todo);
            return todo;
        } else {
            throw new NotFoundException(
                    Response.status(Response.Status.NOT_FOUND).entity(
                            """
                            {
                                "error": "Todo not found",
                                "message": "Todo with id %d does not exist."
                            }
                            """.formatted(id)
                    ).build()
            );
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        Todo.deleteById(id);
    }

}
