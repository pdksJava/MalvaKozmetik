package org.pdks.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.pdks.entity.MailUser;

@Name("mailUserList")
public class MailUserList extends EntityQuery<MailUser>
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -8553462122121239652L;

	public MailUserList()
    {
        setEjbql("select mailUser from MailUser mailUser");
    }
}
