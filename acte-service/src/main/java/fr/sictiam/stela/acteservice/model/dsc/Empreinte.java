/*
 * Copyright
 *   2010 axYus - www.axyus.com
 *   2010 C.Marchand - christophe.marchand@axyus.com
 *
 * This file is part of DSC.
 *
 * DSC is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DSC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with DSC; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package fr.sictiam.stela.acteservice.model.dsc;

/**
 *
 * @author cmarchand
 */
public class Empreinte {
    private String sha1;
    private String md5;

    public Empreinte() {
        super();
    }

    public Empreinte(String md5, String sha1) {
        this();
        this.sha1=sha1;
        this.md5=md5;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    @Override
    public String toString() {
        return "md5 : ".concat(md5).concat("\nsha1 : ").concat(sha1);
    }

}
