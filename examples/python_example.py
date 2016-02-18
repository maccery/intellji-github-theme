from django.shortcuts import render_to_response
from django.views.generic import View
from data import Category, Event
from events.forms import SearchForm


class CategoriesView(View):
    def get(self, request):

        categories = Category().all()

        if categories:
            form = SearchForm(categories)
            return render_to_response('events/categories.html', {'form': form})
        else:
            return render_to_response('events/no_categories_found.html')


class EventView(View):
    def get(self, request, events_id):
        event = Event().get(events_id)

        if event:
            return render_to_response('events/event.html', {'event': event})
        else:
            return render_to_response('events/no_event_found.html')


class ResultsView(View):
    def get(self, request, page_number=1):
        category_ids = []
        if request.GET.get('category1'): category_ids.append(int(request.GET.get('category1')))
        if request.GET.get('category2'): category_ids.append(int(request.GET.get('category2')))
        if request.GET.get('category3'): category_ids.append(int(request.GET.get('category3')))

        data = Event().get_multiple(category_ids, int(page_number))

        if data:
            events = data['events']
            page_count = data['pagination']['page_count']

            # Pass our returned data to the view. page_count is the total number of pages to paginate through
            return render_to_response('events/events.html',
                                      {'events': events, 'range': range(1, page_count), 'page_count': page_count})
        else:
            return render_to_response('events/no_results.html')