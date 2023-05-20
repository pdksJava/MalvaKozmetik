package org.pdks.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.pdks.entity.MailUser;

@Name("mailUserHome")
public class MailUserHome extends EntityHome<MailUser>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -446235364961734092L;
	@RequestParameter Long mailUserId;

    @Override
    public Object getId()
    {
        if (mailUserId == null)
        {
            return super.getId();
        }
        else
        {
            return mailUserId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }

}
