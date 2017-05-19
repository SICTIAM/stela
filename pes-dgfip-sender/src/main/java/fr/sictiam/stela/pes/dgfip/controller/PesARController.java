package fr.sictiam.stela.pes.dgfip.controller;

import fr.sictiam.stela.pes.dgfip.command.CreatePesAr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.model.ConcurrencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static org.axonframework.commandhandling.GenericCommandMessage.asCommandMessage;

/**
 * Created by s.vergon on 16/05/2017.
 */

@RestController
@RequestMapping(value = "/api/pesar")
public class PesARController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesARController.class);

    private CommandBus commandBus;

    @Autowired
     public PesARController(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @RequestMapping( method = RequestMethod.POST)
    public void create(@RequestBody CreatePesAr createpesar, HttpServletResponse response) {
        LOGGER.debug("Got a PES AR flow to create {} {} {}", createpesar.getId(),createpesar.getFileContent(),createpesar.getFileName());
        try {
            //CreatePesCommand createPesCommand = new CreatePesCommand(id);
            commandBus.dispatch(asCommandMessage(createpesar));
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (AssertionError ae) {
            LOGGER.warn("Reception PES AR failed - empty param ? ''");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (CommandExecutionException cex) {
            LOGGER.warn("Add Command FAILED with message: {}", cex.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            if (null != cex.getCause()) {
                LOGGER.warn("Caused by: {} {}", cex.getCause().getClass().getName(), cex.getCause().getMessage());
                if (cex.getCause() instanceof ConcurrencyException) {
                    LOGGER.warn("A duplicate PES AR flow with the same ID [{}] already exists.", createpesar.getId());
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            }
        }

    }
}
