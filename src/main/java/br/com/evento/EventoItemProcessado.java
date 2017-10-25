package br.com.evento;

import org.springframework.batch.item.ItemProcessor;

import br.com.evento.model.Evento;

public class EventoItemProcessado implements ItemProcessor<Evento, Evento> {

	@Override
	public Evento process(Evento evento) throws Exception {
		return evento;
	}

	

}
