package br.com.evento;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import br.com.evento.model.Evento;

@Configuration
@EnableBatchProcessing
public class EventoBatchConfiguration {
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	public DataSource dataSource;
	
	@Bean
	public DataSource dataSource() {
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3308/evento");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		return dataSource;
	}
	
	@Bean
	public JdbcCursorItemReader<Evento> reader() {
		JdbcCursorItemReader<Evento> reader = new JdbcCursorItemReader<Evento>();
		reader.setDataSource(dataSource);
		reader.setSql("SELECT * FROM evento");
		reader.setRowMapper(new UserRowMapper());
		return reader;
		
	}
	
	public class UserRowMapper implements RowMapper<Evento> {

		@Override
		public Evento mapRow(ResultSet rs, int rowNum) throws SQLException {
			Evento evento = new Evento();
			evento.setCodigo(rs.getLong("codigo"));
			evento.setData(rs.getString("data"));
			evento.setHorario(rs.getString("horario"));
			evento.setLocal(rs.getString("local"));
			evento.setNome(rs.getString("nome"));
			return evento;
		}
		
	}
	
	public EventoItemProcessado processado() {
		return new EventoItemProcessado();
	}
	
	@Bean
	public FlatFileItemWriter<Evento> writer() {
		FlatFileItemWriter<Evento> writer = new FlatFileItemWriter<>();
		writer.setResource(new ClassPathResource("evento.csv"));
		writer.setLineAggregator(new DelimitedLineAggregator<Evento>() {{
			setDelimiter(",");
			setFieldExtractor(new BeanWrapperFieldExtractor<Evento>() {{
				setNames(new String[] { "codigo", "data", "horario", "local", "nome"});
			}});
		}});
		return writer;
	}
	
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<Evento, Evento> chunk(10)
				.reader(reader())
				.processor(processado())
				.writer(writer())
				.build();
		
	}
	
	@Bean
	public Job exportarEventojob() {
		return jobBuilderFactory.get("exportarEventojob")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
		
	}
	
	
	
	
	
	
	

}
