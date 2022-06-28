package fr.insalyon.creatis.vip.api.business;

import fr.insalyon.creatis.vip.api.exception.ApiException;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.ConfigurationBusiness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author khalilkes service to signup a user in VIP
 */
@Service
public class ApiUserBusiness {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Environment env;
    private final ConfigurationBusiness configurationBusiness;

    public ApiUserBusiness(Environment env, ConfigurationBusiness configurationBusiness) {
        this.env = env;
        this.configurationBusiness = configurationBusiness;
    }

    /**
     *
     * @param user
     * @param comments
     * @param accountTypes
     * @throws ApiException
     */
    public void signup(User user, String comments, String[] accountTypes) throws ApiException {
        try {
            configurationBusiness
                    .signup(user, comments, true, true, accountTypes);
            logger.info("Signing up with the " + user.getEmail());
        } catch (BusinessException e) {
            throw new ApiException("Signing up Error", e);
        }
    }
}
