/*
 * Copyright (c) 1998, 2015, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package com.sun.tools.doclets.formats.html;

import java.io.*;

import com.sun.javadoc.*;
import com.sun.tools.doclets.formats.html.markup.*;
import com.sun.tools.doclets.internal.toolkit.*;
import com.sun.tools.doclets.internal.toolkit.util.*;

/**
 * Class to generate Tree page for a package. The name of the file generated is
 * "package-tree.html" and it is generated in the respective package directory.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 * @author Atul M Dambalkar
 * @author Bhavesh Patel (Modified)
 */
@Deprecated
public class PackageTreeWriter extends AbstractTreeWriter {

    /**
     * Package for which tree is to be generated.
     */
    protected PackageDoc packagedoc;

    /**
     * The previous package name in the alpha-order list.
     */
    protected PackageDoc prev;

    /**
     * The next package name in the alpha-order list.
     */
    protected PackageDoc next;

    /**
     * Constructor.
     * @throws IOException
     * @throws DocletAbortException
     */
    public PackageTreeWriter(ConfigurationImpl configuration,
                             DocPath path,
                             PackageDoc packagedoc,
                             PackageDoc prev, PackageDoc next)
                      throws IOException {
        super(configuration, path,
              new ClassTree(
                configuration.classDocCatalog.allClasses(packagedoc),
                configuration));
        this.packagedoc = packagedoc;
        this.prev = prev;
        this.next = next;
    }

    /**
     * Construct a PackageTreeWriter object and then use it to generate the
     * package tree page.
     *
     * @param pkg      Package for which tree file is to be generated.
     * @param prev     Previous package in the alpha-ordered list.
     * @param next     Next package in the alpha-ordered list.
     * @param noDeprecated  If true, do not generate any information for
     * deprecated classe or interfaces.
     * @throws DocletAbortException
     */
    public static void generate(ConfigurationImpl configuration,
                                PackageDoc pkg, PackageDoc prev,
                                PackageDoc next, boolean noDeprecated) {
        PackageTreeWriter packgen;
        DocPath path = DocPath.forPackage(pkg).resolve(DocPaths.PACKAGE_TREE);
        try {
            packgen = new PackageTreeWriter(configuration, path, pkg,
                prev, next);
            packgen.generatePackageTreeFile();
            packgen.close();
        } catch (IOException exc) {
            configuration.standardmessage.error(
                        "doclet.exception_encountered",
                        exc.toString(), path.getPath());
            throw new DocletAbortException(exc);
        }
    }

    /**
     * Generate a separate tree file for each package.
     */
    protected void generatePackageTreeFile() throws IOException {
        HtmlTree body = getPackageTreeHeader();
        HtmlTree htmlTree = (configuration.allowTag(HtmlTag.MAIN))
                ? HtmlTree.MAIN()
                : body;
        Content headContent = getResource("doclet.Hierarchy_For_Package",
                utils.getPackageName(packagedoc));
        Content heading = HtmlTree.HEADING(HtmlConstants.TITLE_HEADING, false,
                HtmlStyle.title, headContent);
        Content div = HtmlTree.DIV(HtmlStyle.header, heading);
        if (configuration.packages.size() > 1) {
            addLinkToMainTree(div);
        }
        htmlTree.addContent(div);
        HtmlTree divTree = new HtmlTree(HtmlTag.DIV);
        divTree.addStyle(HtmlStyle.contentContainer);
        addTree(classtree.baseclasses(), "doclet.Class_Hierarchy", divTree);
        addTree(classtree.baseinterfaces(), "doclet.Interface_Hierarchy", divTree);
        addTree(classtree.baseAnnotationTypes(), "doclet.Annotation_Type_Hierarchy", divTree);
        addTree(classtree.baseEnums(), "doclet.Enum_Hierarchy", divTree);
        htmlTree.addContent(divTree);
        if (configuration.allowTag(HtmlTag.MAIN)) {
            body.addContent(htmlTree);
        }
        HtmlTree tree = (configuration.allowTag(HtmlTag.FOOTER))
                ? HtmlTree.FOOTER()
                : body;
        addNavLinks(false, tree);
        addBottom(tree);
        if (configuration.allowTag(HtmlTag.FOOTER)) {
            body.addContent(tree);
        }
        printHtmlDocument(null, true, body);
    }

    /**
     * Get the package tree header.
     *
     * @return a content tree for the header
     */
    protected HtmlTree getPackageTreeHeader() {
        String title = packagedoc.name() + " " +
                configuration.getText("doclet.Window_Class_Hierarchy");
        HtmlTree bodyTree = getBody(true, getWindowTitle(title));
        HtmlTree htmlTree = (configuration.allowTag(HtmlTag.HEADER))
                ? HtmlTree.HEADER()
                : bodyTree;
        addTop(htmlTree);
        addNavLinks(true, htmlTree);
        if (configuration.allowTag(HtmlTag.HEADER)) {
            bodyTree.addContent(htmlTree);
        }
        return bodyTree;
    }

    /**
     * Add a link to the tree for all the packages.
     *
     * @param div the content tree to which the link will be added
     */
    protected void addLinkToMainTree(Content div) {
        Content span = HtmlTree.SPAN(HtmlStyle.packageHierarchyLabel,
                getResource("doclet.Package_Hierarchies"));
        div.addContent(span);
        HtmlTree ul = new HtmlTree (HtmlTag.UL);
        ul.addStyle(HtmlStyle.horizontal);
        ul.addContent(getNavLinkMainTree(configuration.getText("doclet.All_Packages")));
        div.addContent(ul);
    }

    /**
     * Get link for the previous package tree file.
     *
     * @return a content tree for the link
     */
    protected Content getNavLinkPrevious() {
        if (prev == null) {
            return getNavLinkPrevious(null);
        } else {
            DocPath path = DocPath.relativePath(packagedoc, prev);
            return getNavLinkPrevious(path.resolve(DocPaths.PACKAGE_TREE));
        }
    }

    /**
     * Get link for the next package tree file.
     *
     * @return a content tree for the link
     */
    protected Content getNavLinkNext() {
        if (next == null) {
            return getNavLinkNext(null);
        } else {
            DocPath path = DocPath.relativePath(packagedoc, next);
            return getNavLinkNext(path.resolve(DocPaths.PACKAGE_TREE));
        }
    }

    /**
     * Get link to the package summary page for the package of this tree.
     *
     * @return a content tree for the package link
     */
    protected Content getNavLinkPackage() {
        Content linkContent = getHyperLink(DocPaths.PACKAGE_SUMMARY,
                packageLabel);
        Content li = HtmlTree.LI(linkContent);
        return li;
    }
}
