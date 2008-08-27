/*
 * Copyright (c) 2006 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.web.mvc.controller;

import org.genedb.db.dao.SequenceDao;
import org.genedb.db.domain.objects.PolypeptideRegionGroup;
import org.genedb.web.gui.DiagramCache;
import org.genedb.web.gui.ProteinMapDiagram;
import org.genedb.web.gui.RenderedProteinMap;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Looks up a feature by unique name
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class FeatureSequenceController extends PostOrGetFormController {
     private static final Logger logger = Logger.getLogger(FeatureSequenceController.class);

    private SequenceDao sequenceDao;
    private String geneSequenceView;

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException be) throws Exception {

        FeatureSequenceBean fsb = (FeatureSequenceBean) command;

        Feature feature = sequenceDao.getFeatureByUniqueName(fsb.getName(), Feature.class);
        if (feature == null) {
            logger.warn(String.format("Failed to find feature '%s'", fsb.getName()));
            be.reject("no.results");
            return showForm(request, response, be);
        }

        String viewName = geneSequenceView;

        Map<String, Object> model = modelBuilder.prepareFeature(feature);

        return new ModelAndView(viewName, model);
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setGeneSequenceView(String geneSequenceView) {
        this.geneSequenceView = geneSequenceView;
    }



    private ModelBuilder modelBuilder;

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }

    public static class FeatureSequenceBean {
        private String name;
        //private boolean sequenceView = false;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

//        public boolean isSequenceView() {
//            return sequenceView;
//        }
//
//        public void setSequenceView(boolean sequenceView) {
//            this.sequenceView = sequenceView;
//        }

    }
}