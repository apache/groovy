# purpose:
#  to create a PDF file from latex source

latex $1.tex
dvips -Ppdf -t landscape $1.dvi
ps2pdf $1.ps




# p.s. best for debugging use...
#   latex $1.tex; xdvi $1.dvi