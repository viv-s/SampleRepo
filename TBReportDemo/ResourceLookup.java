package org.fstrf.actg.TBOutcomeReport.dao;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.fstrf.security.util.SecurityContextWebAppUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResourceLookup implements IResourceLookup{

    private final static Logger logger = LoggerFactory.getLogger(ResourceLookup.class.getName());

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(getDataSource(SecurityContextWebAppUtil.getDatasourceName()));
    }

    @Override
	public NamedParameterJdbcTemplate getNamedParamJdbcTemplate() {
		return new NamedParameterJdbcTemplate(getDataSource(SecurityContextWebAppUtil.getDatasourceName()));
	}

    @Override
    public DataSource getDataSource(String dataSourceName) {
        try {
            Context context = new InitialContext();
            return (DataSource) context.lookup("jdbc/" + dataSourceName.toLowerCase());
        } catch (NamingException e) {
            throw new DatasourceException("Resource could not be found, looking up project: " + dataSourceName, e);
        }
    }
}
