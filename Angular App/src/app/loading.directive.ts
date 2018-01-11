import { Directive, ElementRef, HostListener, Input, OnChanges, OnInit, Renderer } from '@angular/core';

@Directive({
  selector: '[appLoading]'
})
export class LoadingDirective implements OnChanges {

  @Input('appLoading') loadingStatus: string = "loading";

  constructor(private el: ElementRef, private renderer: Renderer) {
    this.checkStatus();
  }

  ngOnChanges(changes) {
    this.checkStatus();
  }

  checkStatus() {
    if (this.loadingStatus === "loading") {
      this.el.nativeElement.style.display = 'block';
      this.renderer.setElementProperty(this.el.nativeElement, 'innerHTML', '<spanLoading...');
    } else if (this.loadingStatus === "finished") {
      this.el.nativeElement.style.display = 'block';
    }
  }
}
