package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.ConvocationRepository;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.service.exceptions.ConvocationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConvocationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConvocationService.class);

	private final ConvocationRepository convocationRepository;

	@Autowired
	public ConvocationService(ConvocationRepository convocationRepository) {
		this.convocationRepository = convocationRepository;
	}

	public Convocation createOrUpdate(Convocation convocation) {
		return convocationRepository.save(convocation);
	}

	public void delete(Convocation convocation) {
		convocationRepository.delete(convocation);
	}

	public Convocation getByUuid(String uuid) {
		return convocationRepository.findById(uuid).orElseThrow(ConvocationNotFoundException::new);
	}

}
